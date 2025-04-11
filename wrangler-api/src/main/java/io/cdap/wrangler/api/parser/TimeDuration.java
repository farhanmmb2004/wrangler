/*
 * Copyright © 2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package io.cdap.wrangler.api.parser;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class TimeDuration implements Token {
  private final long milliseconds;
  private final String original;

  public TimeDuration(String value) {
    this.original=value;
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
  @Override
  public String toString() {
    return original;
  }
  @Override
  public JsonElement toJson() {
   return new JsonPrimitive(original);
  }
  
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }
  
  @Override
public Object value() {
    return milliseconds;
}

}
