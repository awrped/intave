package de.jpx3.intave.module.feedback;

import de.jpx3.intave.math.MathHelper;
import de.jpx3.intave.module.Module;
import de.jpx3.intave.module.linker.bukkit.BukkitEventSubscription;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import de.jpx3.intave.user.meta.CheckCustomMetadata;
import de.jpx3.intave.user.storage.FeedbackAnalysisStorage;
import de.jpx3.intave.user.storage.LatencyStorage;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

import static de.jpx3.intave.module.feedback.FeedbackAnalysis.FeedbackCategory.*;
import static de.jpx3.intave.module.feedback.FeedbackOptions.*;

public final class FeedbackAnalysis extends Module {
  @BukkitEventSubscription
  public void on(PlayerJoinEvent join) {
    User user = UserRepository.userOf(join.getPlayer());
    user.onStorageReady(storage -> {
      FeedbackAnalysisStorage theStorage = user.storageOf(FeedbackAnalysisStorage.class);
      FeedbackAnalysisMeta meta = metaOf(user);
      for (int i = 0; i < values().length; i++) {
        if (theStorage.accumulatedLatencies() != null && theStorage.counts() != null) {
          meta.latencyAnalysisMap.get(values()[i]).set(theStorage.accumulatedLatencies()[i], theStorage.counts()[i]);
        }
      }
      LatencyStorage latencyStorage = user.storageOf(LatencyStorage.class);
      user.meta().violationLevel().backtrackVL = latencyStorage.backtrackVL;
      meta.fullLatencyAnalysis.importFrom(latencyStorage.latencyBuckets);
    });
  }

  @BukkitEventSubscription
  public void on(PlayerQuitEvent quit) {
    User user = UserRepository.userOf(quit.getPlayer());
    user.onStorageReady(storage -> {
      FeedbackAnalysisStorage theStorage = user.storageOf(FeedbackAnalysisStorage.class);
      FeedbackCategory[] values = values();
      theStorage.setAccumulatedLatencies(new long[values.length]);
      theStorage.setCounts(new long[values.length]);
      FeedbackAnalysisMeta meta = metaOf(user);
      for (int i = 0; i < values.length; i++) {
        theStorage.accumulatedLatencies()[i] = meta.latencyAnalysisMap.get(values[i]).accumulatedLatency;
        theStorage.counts()[i] = meta.latencyAnalysisMap.get(values[i]).count;
      }
      LatencyStorage latencyStorage = user.storageOf(LatencyStorage.class);
      latencyStorage.buckets = LongLatencyAnalysis.LATENCY_BUCKETS;
      latencyStorage.latencyBuckets = new long[LongLatencyAnalysis.LATENCY_BUCKETS];
      System.arraycopy(meta.fullLatencyAnalysis.latencyOccurrences, 0, latencyStorage.latencyBuckets, 0, LongLatencyAnalysis.LATENCY_BUCKETS);
      latencyStorage.backtrackVL = (int) user.meta().violationLevel().backtrackVL;
    });
  }

  public void sentTransaction(User user, FeedbackRequest<?> request) {
    FeedbackAnalysisMeta meta = metaOf(user);
    // unimportant
  }

//  private UserLocal<File> latencyAnalysisFile = UserLocal.withInitial(user -> new File(IntavePlugin.singletonInstance().dataFolder(), user.id() + "-latency.csv"));

  public void receivedTransaction(User user, FeedbackRequest<?> request) {
    FeedbackAnalysisMeta meta = metaOf(user);
    int requestOptions = FeedbackOptions.onlyTracerOptions(request.options());
    FeedbackCategory category = fromFeedbackOptions(requestOptions);
    Map<FeedbackCategory, LatencyAnalysis> latencyAnalysisMap = meta.latencyAnalysisMap;
    LatencyAnalysis analysis = latencyAnalysisMap.get(category);
    LatencyAnalysis combatNearAnalysis = latencyAnalysisMap.get(ENTITY_NEAR);
    long transactionDelay = request.passedTime();
    double averageGeneral = meta.shortLatencyAnalysis.averageLatency();

    meta.fullLatencyAnalysis.addLatency(transactionDelay);
    meta.shortLatencyAnalysis.addLatency(transactionDelay);

    boolean movedCloser = FeedbackOptions.matches(TRACER_ENTITY_MOVED_CLOSER, requestOptions);
    boolean movedFarther = FeedbackOptions.matches(TRACER_ENTITY_MOVED_FARTHER, requestOptions);
    boolean deviating = Math.abs(transactionDelay - averageGeneral) > 100;

    meta.deviationTransactionType
      .computeIfAbsent(DeviationCategory.fromFeedbackOptions(requestOptions), DeviationFrequency::new)
      .add(deviating);

    if (System.currentTimeMillis() - meta.lastDeviationRecheck > 4000) {
      meta.lastDeviationRecheck = System.currentTimeMillis();
      recheckDeviations(user);
    }

    if (category == ENTITY_NEAR && user.meta().attack().recentlyAttacked(1000) && transactionDelay > 100) {
      double probability = meta.fullLatencyAnalysis.biasedProbabilityOf(transactionDelay, 300);
      // 1 in 150_000
      //                0.0000033976731
      if (probability < 0.0000033976731) {
        meta.suspiciousLatencies.add(new FeedbackAnalysisMeta.LatencyInfo(transactionDelay, movedFarther));
      }
    }

    if (category == ENTITY_NEAR ||
      System.currentTimeMillis() - combatNearAnalysis.lastEntry() > 1500) {
      analysis.addLatency(transactionDelay);
    }
  }

  private void recheckDeviations(User user) {
    FeedbackAnalysisMeta meta = metaOf(user);
    {
      DeviationCategory[] values = DeviationCategory.values();
      long totalNat = 0, totalDev = 0;
      for (DeviationCategory value : values) {
        DeviationFrequency data = meta.deviationTransactionType.get(value);
        if (data == null) {
          continue;
        }
        totalNat += data.natural();
        totalDev += data.deviating();
      }

      for (DeviationCategory category : values) {
        DeviationFrequency data = meta.deviationTransactionType.get(category);
        if (data == null) {
          continue;
        }
        if (category == DeviationCategory.ENTITY_NEAR_FARING && totalDev > 0 && totalNat > 0) {
          double naturalFrequency = data.natural() / (double) totalNat;
          double deviatingFrequency = data.deviating() / (double) totalDev;
          double rate = deviatingFrequency / naturalFrequency;
          if (rate > 3 && data.natural() > 20 && data.deviating() > 10) {
            meta.lastFrequencyMismatchReport = System.currentTimeMillis();
            meta.lastDeviationMessage = "nat: " + MathHelper.formatDouble(naturalFrequency * 100, 2) +
              " dev: " + MathHelper.formatDouble(deviatingFrequency * 100, 2) + "@" + data.natural()+"/"+data.deviating();
          }
        }
      }
      if (totalNat + totalDev > 10000) {
        for (DeviationCategory category : values) {
          DeviationFrequency data = meta.deviationTransactionType.get(category);
          if (data == null) {
            continue;
          }
          data.downscale(1.3333);
        }
      }
    }
  }

  public boolean recentlyDetectedForFreqMisrep(User user) {
    return System.currentTimeMillis() - metaOf(user).lastFrequencyMismatchReport < 8000;
  }

  public String lastFreqMisrepMessage(User user) {
    return metaOf(user).lastDeviationMessage;
  }

  public long entityLatencyDiscrepancy(User user) {
    FeedbackAnalysisMeta meta = metaOf(user);
    LatencyAnalysis general = meta.latencyAnalysisMap.get(GENERAL);
    LatencyAnalysis entityNear = meta.latencyAnalysisMap.getOrDefault(ENTITY_NEAR, general);
    LatencyAnalysis entityFar = meta.latencyAnalysisMap.getOrDefault(ENTITY_FAR, general);
    if (general == null) {
      return 0;
    }
    long generalLatency = general.averageLatency();
    long entityFarLatency = entityFar.averageLatency();
    if (entityFarLatency > 0) {
      generalLatency = (generalLatency + entityFarLatency) / 2;
    }
    if (generalLatency > 200) {
      return -3;
    }
    long entityNearLatency = entityNear.averageLatency();
//    long entityNearCombatLatency = meta.latencyAnalysisMap.getOrDefault(ENTITY_NEAR_COMBAT, general).averageLatency();
//    entityNearLatency = (entityNearLatency + entityNearCombatLatency) / 2;
    if (entityNear.count < 100) {
      return -1;
    } else if (general.count < 100) {
      return -2;
    }/* else if (entityFar.count < 100) {
      return -3;
    }*/
    return entityNearLatency - generalLatency;
  }

  public long entityNearLatency(User user) {
    return metaOf(user).latencyAnalysisMap.get(ENTITY_NEAR).averageLatency();
  }

  public long generalLatency(User user) {
    return metaOf(user).latencyAnalysisMap.get(GENERAL).averageLatency();
  }

  public List<FeedbackAnalysisMeta.LatencyInfo> suspiciousLatencies(User user) {
    return metaOf(user).suspiciousLatencies;
  }

  public double meanLatency(User user) {
    return metaOf(user).fullLatencyAnalysis.mean();
  }

  public double stdDev(User user) {
    return metaOf(user).fullLatencyAnalysis.biasedStdDev(300);
  }

  public double latencyProbability(User user, long latency) {
    return metaOf(user).fullLatencyAnalysis.biasedProbabilityOf(latency, 300);
  }

  public FeedbackAnalysisMeta metaOf(User user) {
    return (FeedbackAnalysisMeta) user.checkMetadata(FeedbackAnalysisMeta.class);
  }

//  public Map<DeviationCategory, DeviationFrequency> deviationFrequencyDataOf(User user) {
//    return metaOf(user).deviationTransactionType;
//  }

  public static class FeedbackAnalysisMeta extends CheckCustomMetadata {
    private final Map<FeedbackCategory, LatencyAnalysis> latencyAnalysisMap = new HashMap<>();
    {
      latencyAnalysisMap.put(GENERAL, new LatencyAnalysis(500));
      latencyAnalysisMap.put(ENTITY_FAR, new LatencyAnalysis(500));
      latencyAnalysisMap.put(ENTITY_NEAR, new LatencyAnalysis(250));
      latencyAnalysisMap.put(ENTITY_NEAR_COMBAT, new LatencyAnalysis(250));
    }
    private final LatencyAnalysis shortLatencyAnalysis = new LatencyAnalysis(500);
    private final LongLatencyAnalysis fullLatencyAnalysis = new LongLatencyAnalysis();
    private final Map<DeviationCategory, DeviationFrequency> deviationTransactionType = new HashMap<>();
    private long lastDeviationRecheck = System.currentTimeMillis();
    private long lastFrequencyMismatchReport = System.currentTimeMillis();
    private String lastDeviationMessage = "";
    private final List<LatencyInfo> suspiciousLatencies = new ArrayList<>(50);

    public static class LatencyInfo {
      private final long latency;
      private final long time = System.currentTimeMillis();
      private final boolean faring;

      public LatencyInfo(long latency, boolean faring) {
        this.latency = latency;
        this.faring = faring;
      }

      public boolean faring() {
        return faring;
      }

      public long latency() {
        return latency;
      }

      public long issued() {
        return time;
      }
    }
  }

  public static class DeviationFrequency {
    private final DeviationCategory feedbackKey;
    private long natualOccurences;
    private long deviatingOccurences;

    public DeviationFrequency(DeviationCategory feedbackKey) {
      this.feedbackKey = feedbackKey;
    }

    public void add(boolean deviating) {
      if (deviating) {
        deviatingOccurences++;
      } else {
        natualOccurences++;
      }
    }

    public long natural() {
      return natualOccurences;
    }

    public long deviating() {
      return deviatingOccurences;
    }

    public void downscale(double factor) {
      natualOccurences = (long) (natualOccurences / factor);
      deviatingOccurences = (long) (deviatingOccurences / factor);
    }
  }

  public enum DeviationCategory {
    GENERAL,
    ENTITY_NEAR_NEARING,
    ENTITY_NEAR_FARING,
    ENTITY_FAR_NEARING,
    ENTITY_FAR_FARING

    ;

    public static DeviationCategory fromFeedbackOptions(int options) {
      boolean movedCloser = FeedbackOptions.matches(TRACER_ENTITY_MOVED_CLOSER, options);
      boolean movedFarther = FeedbackOptions.matches(TRACER_ENTITY_MOVED_FARTHER, options);

      boolean near = FeedbackOptions.matches(TRACER_ENTITY_IS_NEAR, options);
      boolean far = FeedbackOptions.matches(TRACER_ENTITY_IS_FAR, options);

      boolean general = !near && !far;

      if (general) {
        return GENERAL;
      } else if (near && movedCloser) {
        return ENTITY_NEAR_NEARING;
      } else if (near && movedFarther) {
        return ENTITY_NEAR_FARING;
      } else if (far && movedCloser) {
        return ENTITY_FAR_NEARING;
      } else if (far && movedFarther) {
        return ENTITY_FAR_FARING;
      }
      return GENERAL;
    }
  }

  public static class LatencyAnalysis {
    private long accumulatedLatency;
    private long count;
    private long lastEntry;
    private final long size;

    public LatencyAnalysis(long size) {
      this.size = size;
    }

    public void set(long accumulatedLatency, long count) {
      this.accumulatedLatency = accumulatedLatency;
      this.count = count;
    }

    public void addLatency(long latency) {
      latency = Math.min(latency, 1000);
      accumulatedLatency += latency;
      count++;
      if (count > size) {
        accumulatedLatency /= 2;
        count /= 2;
      }
      lastEntry = System.currentTimeMillis();
    }

    public long averageLatency() {
      if (count == 0) {
        return 0;
      }
      return accumulatedLatency / count;
    }

    public double mean() {
      return (double) accumulatedLatency / count;
    }

    public long lastEntry() {
      return lastEntry;
    }
  }

  public static class LongLatencyAnalysis {
    private static final int MAX_LATENCY = 1000;
    private static final int LATENCY_BUCKETS = 100;
    private long[] latencyOccurrences = new long[LATENCY_BUCKETS + 1];
    private long size = 0;

    public boolean addLatency(long latency) {
      if (latency > MAX_LATENCY || latency < 0) {
        return false;
      }
      latencyOccurrences[(int) asDiscrete(latency)]++;
      size++;
      if (size > 9999) {
        // divide all by 2
        for (int i = 0; i < LATENCY_BUCKETS; i++) {
          latencyOccurrences[i] >>= 1;
        }
        size >>= 1;
      }
      return true;
    }

    private long asDiscrete(long latency) {
      return Math.min(latency, MAX_LATENCY - 1) / (MAX_LATENCY / LATENCY_BUCKETS);
    }

    public double mean() {
      long sum = 0;
      int scalingFactor = MAX_LATENCY / LATENCY_BUCKETS;
      for (int i = 0; i < LATENCY_BUCKETS; i++) {
        sum += latencyOccurrences[i] * i * scalingFactor;
      }
      return (double) sum / size;
    }

    public long stdDev() {
      return stdDev(mean());
    }

    public long stdDev(double mean) {
      double sum = 0;
      int scalingFactor = MAX_LATENCY / LATENCY_BUCKETS;
      for (int i = 0; i < LATENCY_BUCKETS; i++) {
        sum += Math.pow(i*scalingFactor - mean, 2) * latencyOccurrences[i];
      }
      return (long) Math.sqrt(sum / size);
    }

    public long variance() {
      return (long) Math.pow(stdDev(), 2);
    }

    public double biasedStdDev(double requiredDistance) {
      return biasedStdDev(mean(), requiredDistance);
    }

    public double biasedStdDev(double mean, double requiredDistance) {
      double sum = 0;
      int scalingFactor = MAX_LATENCY / LATENCY_BUCKETS;
      for (int i = 0; i < LATENCY_BUCKETS; i++) {
        double dist = Math.abs(i*scalingFactor - mean);
        double weight = Math.exp(-dist / requiredDistance);
        sum += Math.pow(dist, 2) * latencyOccurrences[i] * weight;
      }
      return Math.max(Math.sqrt(sum / size), 25);
    }

    public double likelihoodOf(long latency) {
      return latencyOccurrences[(int) asDiscrete(latency)] / (double) size;
    }

    public double probabilityOf(long latency) {
      double mean = mean();
      double stdDev = stdDev(mean);
      return Math.exp(-Math.pow(latency - mean, 2) / (2 * Math.pow(stdDev, 2))) / (stdDev * Math.sqrt(2 * Math.PI));
    }

    public double biasedProbabilityOf(long latency, double biasDistance) {
      double mean = mean();
      if (latency < mean) {
        return 100;
      }
      double stdDev = biasedStdDev(mean, biasDistance);
      if (latency < mean + stdDev) {
        return 100;
      }
      return Math.exp(-Math.pow(latency - mean, 2) / (2 * Math.pow(stdDev, 2))) / (stdDev * Math.sqrt(2 * Math.PI));
    }

    public void clear() {
      Arrays.fill(latencyOccurrences, 0);
      size = 0;
    }

    public void importFrom(long[] latencyOccurrences) {
      if (latencyOccurrences == null || latencyOccurrences.length != LATENCY_BUCKETS) {
        // nothing to import
        return;
      }
      this.latencyOccurrences = new long[LATENCY_BUCKETS];
      System.arraycopy(latencyOccurrences, 0, this.latencyOccurrences, 0, LATENCY_BUCKETS);
      size = Arrays.stream(latencyOccurrences).sum();
    }
  }

  public enum FeedbackCategory {
    GENERAL,
    ENTITY_FAR,
    ENTITY_NEAR,
    ENTITY_NEAR_COMBAT

    ;

    public static FeedbackCategory fromFeedbackOptions(int options) {
      if (matches(TRACER_ENTITY_IS_FAR, options)) {
        return ENTITY_FAR;
      }
      if (matches(TRACER_ENTITY_IS_NEAR, options)) {
        return ENTITY_NEAR;
      }
      if (matches(TRACER_ENTITY_IS_NEAR_IN_COMBAT, options)) {
        return ENTITY_NEAR_COMBAT;
      }
      return GENERAL;
    }
  }
}
