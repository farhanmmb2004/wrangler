package io.cdap.wrangler.api.parser;

public class ByteSize extends Token {
  private final long bytes;

  public ByteSize(String value) {
    super(value);
    this.bytes = parse(value);
  }

  private long parse(String val) {
    String lower = val.toLowerCase().trim();
    if (lower.endsWith("kb")) {
      return (long) (Double.parseDouble(lower.replace("kb", "")) * 1024);
    } else if (lower.endsWith("mb")) {
      return (long) (Double.parseDouble(lower.replace("mb", "")) * 1024 * 1024);
    } else if (lower.endsWith("gb")) {
      return (long) (Double.parseDouble(lower.replace("gb", "")) * 1024 * 1024 * 1024);
    } else if (lower.endsWith("tb")) {
      return (long) (Double.parseDouble(lower.replace("tb", "")) * 1024L * 1024 * 1024 * 1024);
    } else if (lower.endsWith("b")) {
      return (long) Double.parseDouble(lower.replace("b", ""));
    } else {
      throw new IllegalArgumentException("Unrecognized byte unit: " + val);
    }
  }

  public long getBytes() {
    return bytes;
  }
}
