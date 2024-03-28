package de.jpx3.intave.math;

import java.util.Arrays;
import java.util.List;

public class Histogram {
  private final double start;
  private final double end;
  private final double step;
  private final int[] bins;
  private final int limit;
  private double total;

  public Histogram(double start, double end, double step, int limit) {
    this.start = start;
    this.end = end;
    this.step = step;
    this.bins = new int[(int)Math.ceil((end - start) / step)];
    this.total = 0;
    this.limit = limit;
  }

  public void add(double value) {
    if (value < start || value > end) {
      return;
    }
    int index = (int)Math.floor((value - start) / step);
    if (index < 0 || index >= bins.length) {
      return;
    }
    bins[index]++;
    total++;

    if (total > limit) {
      total /= 2;
      for (int i = 0; i < bins.length; i++) {
        bins[i] /= 2;
      }
    }
  }

  public List<String> plot() {
    int max = 0;
    for (int bin : bins) {
      max = Math.max(max, bin);
    }

    int height = 10;
    int width = 30;

    String[] lines = new String[height];
    Arrays.fill(lines, "");

    for (int bin : bins) {
      int count = (int) Math.ceil((double) bin / max * width);
      for (int j = 0; j < height; j++) {
        lines[j] += count > j ? "#" : " ";
      }
    }

    return Arrays.asList(lines);
  }

  public void forceAdd(double value) {
    add(Math.max(start, Math.min(end, value)));
  }

  public double mean() {
    double sum = 0;
    double count = 0;
    for (int i = 0; i < bins.length; i++) {
      sum += bins[i] * (start + i * step);
      count += bins[i];
    }
    return sum / count;
  }

  public double variance() {
    double mean = mean();
    double sum = 0;
    double count = 0;
    for (int i = 0; i < bins.length; i++) {
      sum += bins[i] * Math.pow((start + i * step) - mean, 2);
      count += bins[i];
    }
    return sum / count;
  }

  public double standardDeviation() {
    return Math.sqrt(variance());
  }

  public double likelihood(double value) {
    if (value < start || value > end) {
      return 0;
    }
    int index = (int)Math.floor((value - start) / step);
    if (index < 0 || index >= bins.length) {
      return 0;
    }
    return (double)bins[index] / total;
  }

  public double likelihood(double start, double end) {
    double sum = 0;
    for (int i = (int)Math.floor((start - this.start) / step); i < (int)Math.ceil((end - this.start) / step); i++) {
      sum += bins[i];
    }
    return sum / total;
  }

  public double normalProbability(double value) {
    return Math.exp(-Math.pow(value - mean(), 2) / (2 * variance())) / Math.sqrt(2 * Math.PI * variance());
  }

  public int binCount() {
    return bins.length;
  }

  public int size() {
    return (int) total;
  }

  public void clear() {
    Arrays.fill(bins, 0);
    total = 0;
  }
}
