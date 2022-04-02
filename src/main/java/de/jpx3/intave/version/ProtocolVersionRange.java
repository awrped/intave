package de.jpx3.intave.version;

public class ProtocolVersionRange implements Comparable<ProtocolVersionRange> {
  private final int from;
  private final int to;
  private final String version;

  public ProtocolVersionRange(int from, int to, String version) {
    this.from = from;
    this.to = to;
    this.version = version;
  }

  public int from() {
    return from;
  }

  public int to() {
    return to;
  }

  public boolean has(int version) {
    return from <= version && version <= to;
  }

  public String version() {
    return version;
  }

  public int compareTo(ProtocolVersionRange other) {
    return to - other.to;
  }

  @Override
  public String toString() {
    return "version " + version + " from " + from + " to " + to;
  }
}
