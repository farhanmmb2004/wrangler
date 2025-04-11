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
 /**
  * A token representing byte size values like 10KB, 5MB, etc.
  */
 public class ByteSize implements Token {
   private final long bytes;
   private final String original;
 
   public ByteSize(String value) {
     this.original = value;
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
    return TokenType.BYTE_SIZE;
   }
   
   @Override
   public Object value() {
    return bytes;
   }

 }
 