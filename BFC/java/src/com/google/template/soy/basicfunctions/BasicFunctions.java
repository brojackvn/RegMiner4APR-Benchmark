/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.template.soy.basicfunctions;

import com.google.common.collect.ImmutableList;
import com.google.template.soy.plugin.restricted.SoySourceFunction;

/** Lists all functions in this package. */
public class BasicFunctions {

  private BasicFunctions() {}

  public static ImmutableList<SoySourceFunction> functions() {
    return ImmutableList.of(
        new CeilingFunction(),
        new ConcatListsFunction(),
        new FloorFunction(),
        new HtmlToTextFunction(),
        new IsNonnullFunction(),
        new IsNullFunction(),
        new JoinFunction(),
        new KeysFunction(),
        new LegacyObjectMapToMapFunction(),
        new LengthFunction(),
        new ListContainsFunction(),
        new ListIndexOfFunction(),
        new ListSliceMethod(),
        new MapKeysFunction(),
        new MapToLegacyObjectMapFunction(),
        new MaxFunction(),
        new MinFunction(),
        new NumberListSortMethod(),
        new ParseFloatFunction(),
        new ParseIntFunction(),
        new RandomIntFunction(),
        new RangeFunction(),
        new RoundFunction(),
        new SqrtFunction(),
        new StrContainsFunction(),
        new StrEndsWithMethod(),
        new StrIndexOfFunction(),
        new StrLenFunction(),
        new StrSmsUriToUriFunction(),
        new StrSplitMethod(),
        new StrStartsWithMethod(),
        new StrSubFunction(),
        new StrToAsciiLowerCaseFunction(),
        new StrToAsciiUpperCaseFunction(),
        new StrTrimMethod(),
        new StringListSortMethod());
  }
}
