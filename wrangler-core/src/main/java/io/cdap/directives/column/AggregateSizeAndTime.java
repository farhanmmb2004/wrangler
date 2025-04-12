/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

 package io.cdap.directives.column;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.Executor;

import java.util.Collections;
import java.util.List;

@Plugin(type = "directives")
@Name("aggregate-size-time")
@Categories(categories = {"column"})
@Description("Aggregates total byte size and total/average time duration.")
public class AggregateSizeAndTime implements Directive, Executor<List<Row>, List<Row>> {
  private String sizeSourceCol;
  private String timeSourceCol;
  private String sizeTargetCol;
  private String timeTargetCol;
  private String timeAggType;

  private static final String SIZE_TOTAL_KEY = "_agg_total_bytes";
  private static final String TIME_TOTAL_KEY = "_agg_total_millis";
  private static final String ROW_COUNT_KEY = "_agg_row_count";

  @Override
  public UsageDefinition define() {
    // Fix: Create the builder with the directive name as parameter
    UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-size-time");
    
    // Use the builder's define methods
    builder.define("sizeSourceCol", TokenType.COLUMN_NAME);
    builder.define("timeSourceCol", TokenType.COLUMN_NAME);
    builder.define("sizeTargetCol", TokenType.COLUMN_NAME);
    builder.define("timeTargetCol", TokenType.COLUMN_NAME);
    builder.define("timeAggType", TokenType.IDENTIFIER, true); // Make it optional
    
    // Return the built UsageDefinition
    return builder.build();
  }

  @Override
  public void initialize(Arguments arguments) throws DirectiveParseException {
    this.sizeSourceCol = ((ColumnName) arguments.value("sizeSourceCol")).value();
    this.timeSourceCol = ((ColumnName) arguments.value("timeSourceCol")).value();
    this.sizeTargetCol = ((ColumnName) arguments.value("sizeTargetCol")).value();
    this.timeTargetCol = ((ColumnName) arguments.value("timeTargetCol")).value();
    
    // Fix for optional parameter handling
    try {
      // Get the optional timeAggType value
      Object timeAggTypeValue = arguments.value("timeAggType");
      this.timeAggType = (timeAggTypeValue != null) ? 
                         timeAggTypeValue.toString().toLowerCase() : 
                         "total";
    } catch (Exception e) {
      // Default if any error occurs
      this.timeAggType = "total";
    }
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    TransientStore store = context.getTransientStore();
    TransientVariableScope scope = TransientVariableScope.GLOBAL;

    // Get values with null checks
    Long sizeTotalObj = (Long) store.get(SIZE_TOTAL_KEY);
    Long timeTotalObj = (Long) store.get(TIME_TOTAL_KEY);
    Long rowCountObj = (Long) store.get(ROW_COUNT_KEY);
    
    long sizeTotal = sizeTotalObj != null ? sizeTotalObj : 0L;
    long timeTotal = timeTotalObj != null ? timeTotalObj : 0L;
    long rowCount = rowCountObj != null ? rowCountObj : 0L;

    for (Row row : rows) {
      Object sizeObj = row.getValue(sizeSourceCol);
      Object timeObj = row.getValue(timeSourceCol);

      if (sizeObj instanceof ByteSize && timeObj instanceof TimeDuration) {
        sizeTotal += ((ByteSize) sizeObj).getBytes();
        timeTotal += ((TimeDuration) timeObj).getMilliseconds();
        rowCount++;
      } else {
        throw new DirectiveExecutionException("Invalid types: expected ByteSize and TimeDuration.");
      }
    }

    // Set values with scope parameter
    store.set(scope, SIZE_TOTAL_KEY, sizeTotal);
    store.set(scope, TIME_TOTAL_KEY, timeTotal);
    store.set(scope, ROW_COUNT_KEY, rowCount);

    return Collections.emptyList();
  }

  @Override
  public void destroy() {
    // Cleanup code if needed
  }

  // This method is for final aggregation
  public List<Row> finalize(ExecutorContext context) {
    TransientStore store = context.getTransientStore();

    // Get values with null checks
    Long sizeTotalObj = (Long) store.get(SIZE_TOTAL_KEY);
    Long timeTotalObj = (Long) store.get(TIME_TOTAL_KEY);
    Long rowCountObj = (Long) store.get(ROW_COUNT_KEY);
    
    long sizeTotal = sizeTotalObj != null ? sizeTotalObj : 0L;
    long timeTotal = timeTotalObj != null ? timeTotalObj : 0L;
    long rowCount = rowCountObj != null ? rowCountObj : 0L;

    long timeResult = timeAggType.equals("average") && rowCount > 0 ? timeTotal / rowCount : timeTotal;

    Row result = new Row();
    result.add(sizeTargetCol, sizeTotal);
    result.add(timeTargetCol, timeResult);

    return Collections.singletonList(result);
  }
}