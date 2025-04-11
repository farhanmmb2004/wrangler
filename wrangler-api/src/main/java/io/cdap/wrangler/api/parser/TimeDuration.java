package io.cdap.wrangler.api.parser;

public class TimeDuration extends Token {
  private final long milliseconds;

  public TimeDuration(String value) {
    super(value);
    this.milliseconds = parse(value);
  }

  private long parse(String val) {
    String lower = val.toLowerCase().trim();
    if (lower.endsWith("ms")) {
      return (long) Double.parseDouble(lower.replace("ms", ""));
    } else if (lower.endsWith("s")) {
      return (long) (Double.parseDouble(lower.replace("s", "")) * 1000);
    } else if (lower.endsWith("m")) {
      return (long) (Double.parseDouble(lower.replace("m", "")) * 60 * 1000);
    } else if (lower.endsWith("h")) {
      return (long) (Double.parseDouble(lower.replace("h", "")) * 60 * 60 * 1000);
    } else if (lower.endsWith("ns")) {
      return (long) (Double.parseDouble(lower.replace("ns", "")) / 1_000_000.0);
    } else {
      throw new IllegalArgumentException("Unrecognized time unit: " + val);
    }
  }

  public long getMilliseconds() {
    return milliseconds;
  }
}
