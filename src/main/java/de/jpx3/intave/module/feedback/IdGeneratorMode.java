package de.jpx3.intave.module.feedback;

import de.jpx3.intave.security.LicenseAccess;
import de.jpx3.intave.user.User;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public enum IdGeneratorMode {
  FULL_RANDOM((user, last) -> (user.meta().connection().feedbackUserKeyRandom.nextInt(FeedbackSender.MAX_USER_KEY - FeedbackSender.MIN_USER_KEY) + FeedbackSender.MIN_USER_KEY)),
  POSITIVE_RANDOM((user, last) -> (user.meta().connection().feedbackUserKeyRandom.nextInt(FeedbackSender.MAX_USER_KEY))),
  NEGATIVE_RANDOM(POSITIVE_RANDOM.generator.andThen(integer -> -integer)),
//  INTAVE_VANILLA_POSITIVE((user, last) -> {
//    ConnectionMetadata connection = user.meta().connection();
//    FeedbackQueue feedbackQueue = connection.feedbackQueue();
//    // With a 50% probability, we will start at feedbackQueue.size() to reduce the chance of clogging.
//    if (connection.generatorRunningNum == 0) {
//      connection.generatorRunningNum = feedbackQueue.size() > 500 ? ThreadLocalRandom.current().nextInt(ThreadLocalRandom.current().nextInt(feedbackQueue.size()), feedbackQueue.size()) : 1;
//    }
//    return outOfBounds(last) ? 0 : connection.generatorRunningNum++;
//  }),
//  INTAVE_VANILLA_NEGATIVE((user, last) -> {
//    ConnectionMetadata connection = user.meta().connection();
//    FeedbackQueue feedbackQueue = connection.feedbackQueue();
//    // With a 50% probability, we will start at feedbackQueue.size() to reduce the chance of clogging.
//    if (connection.generatorRunningNum == 0) {
//      connection.generatorRunningNum = feedbackQueue.size() > 500 ? -1 * ThreadLocalRandom.current().nextInt(ThreadLocalRandom.current().nextInt(feedbackQueue.size()), feedbackQueue.size()) : -1;
//    }
//    return outOfBounds(last) ? 0 : connection.generatorRunningNum--;
//  }),
  SPOOF_GRIM((user, last) -> outOfBounds(last) ? 0 : (last - 1)),
  SPOOF_VULCAN((user, last) -> outOfBounds(last) || last > 0 ? -23767 : (last + 1)),
  SPOOF_VERUS((user, last) -> outOfBounds(last) ? ThreadLocalRandom.current().nextInt(-1000, 1000) : (last > 0 ? last + 1 : last - 1)),
  SPOOF_KARHU((user, last) -> outOfBounds(last) ? -3000 : (last - 1)),
  SPOOF_POLAR((user, last) -> outOfBounds(last) ? -250 : (last - 1))

  ;

  private final BiFunction<? super User, ? super Integer, Integer> generator;

  IdGeneratorMode(BiFunction<? super User, ? super Integer, Integer> generator) {
    this.generator = generator;
  }

  public int generate(User user, int lastId) {
    return this.generator.apply(user, lastId);
  }

  private static boolean outOfBounds(int id) {
    return id <= FeedbackSender.MIN_USER_KEY || id >= FeedbackSender.MAX_USER_KEY;
  }

  public static IdGeneratorMode highestCompatibility() {
    return NEGATIVE_RANDOM;
  }

  public static IdGeneratorMode mostStableAndRobust() {
    return FULL_RANDOM;
  }

  private static IdGeneratorMode modeOfTheDayCache;

  public static IdGeneratorMode modeOfTheDay() {
    if (modeOfTheDayCache == null) {
      Date date = new Date();
      Random random = new Random(date.getDay()*40 + LicenseAccess.network().hashCode() + date.getMonth()*24 + date.getYear()*5);
      modeOfTheDayCache = values()[random.nextInt(values().length)];
      // the modes just suck tbh
    }
    return modeOfTheDayCache;
  }
}
