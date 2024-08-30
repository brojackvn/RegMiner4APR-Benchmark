/*
 * Copyright 2017 Google Inc.
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

package com.google.template.soy.sharedpasses.render;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.template.soy.base.SourceLocation;
import com.google.template.soy.data.SanitizedContent;
import com.google.template.soy.data.SoyDict;
import com.google.template.soy.data.SoyLegacyObjectMap;
import com.google.template.soy.data.SoyList;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueConverterUtility;
import com.google.template.soy.data.UnsafeSanitizedContentOrdainer;
import com.google.template.soy.data.restricted.BooleanData;
import com.google.template.soy.data.restricted.FloatData;
import com.google.template.soy.data.restricted.IntegerData;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.types.AnyType;
import com.google.template.soy.types.BoolType;
import com.google.template.soy.types.FloatType;
import com.google.template.soy.types.IntType;
import com.google.template.soy.types.LegacyObjectMapType;
import com.google.template.soy.types.ListType;
import com.google.template.soy.types.NullType;
import com.google.template.soy.types.SanitizedType;
import com.google.template.soy.types.SanitizedType.HtmlType;
import com.google.template.soy.types.SoyType;
import com.google.template.soy.types.StringType;
import com.google.template.soy.types.UnknownType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TofuTypeChecksTest {
  private static final NullData NULL_DATA = NullData.INSTANCE;
  private static final BooleanData BOOLEAN_DATA = BooleanData.TRUE;
  private static final StringData STRING_DATA = StringData.forValue("foo");
  private static final IntegerData INTEGER_DATA = IntegerData.forValue(2);
  private static final FloatData FLOAT_DATA = FloatData.forValue(2.0);
  private static final SoyList LIST_DATA = SoyValueConverterUtility.newList();
  private static final SoyLegacyObjectMap MAP_DATA = SoyValueConverterUtility.newDict();
  private static final SoyDict DICT_DATA = SoyValueConverterUtility.newDict();

  private static SanitizedContent htmlData =
      UnsafeSanitizedContentOrdainer.ordainAsSafe("html", SanitizedContent.ContentKind.HTML, null);
  private static SanitizedContent attributesData =
      UnsafeSanitizedContentOrdainer.ordainAsSafe(
          "attrs", SanitizedContent.ContentKind.ATTRIBUTES, null);
  private static SanitizedContent cssData =
      UnsafeSanitizedContentOrdainer.ordainAsSafe("css", SanitizedContent.ContentKind.CSS, null);
  private static SanitizedContent uriData =
      UnsafeSanitizedContentOrdainer.ordainAsSafe("uri", SanitizedContent.ContentKind.URI, null);
  private static SanitizedContent trustedResourceUriData =
      UnsafeSanitizedContentOrdainer.ordainAsSafe(
          "trusted_resource_uri", SanitizedContent.ContentKind.TRUSTED_RESOURCE_URI, null);
  private static SanitizedContent jsData =
      UnsafeSanitizedContentOrdainer.ordainAsSafe("js", SanitizedContent.ContentKind.JS, null);

  @Before
  public void setUp() {
    setUpSanitizedData();
  }

  private void setUpSanitizedData() {
    htmlData =
        UnsafeSanitizedContentOrdainer.ordainAsSafe(
            "html", SanitizedContent.ContentKind.HTML, null);
    attributesData =
        UnsafeSanitizedContentOrdainer.ordainAsSafe(
            "attrs", SanitizedContent.ContentKind.ATTRIBUTES, null);
    cssData =
        UnsafeSanitizedContentOrdainer.ordainAsSafe("css", SanitizedContent.ContentKind.CSS, null);
    uriData =
        UnsafeSanitizedContentOrdainer.ordainAsSafe("uri", SanitizedContent.ContentKind.URI, null);
    trustedResourceUriData =
        UnsafeSanitizedContentOrdainer.ordainAsSafe(
            "trusted_resource_uri", SanitizedContent.ContentKind.TRUSTED_RESOURCE_URI, null);
    jsData =
        UnsafeSanitizedContentOrdainer.ordainAsSafe("js", SanitizedContent.ContentKind.JS, null);
  }

  @Test
  public void testAnyTypeIsInstance() {
    assertIsInstance(
        AnyType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);
  }

  @Test
  public void testUnknownTypeIsInstance() {
    assertIsInstance(
        UnknownType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);
  }

  @Test
  public void testNullTypeIsInstance() {
    assertIsInstance(NullType.getInstance(), NULL_DATA);
    assertIsNotInstance(
        NullType.getInstance(),
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);
  }

  @Test
  public void testBoolTypeIsInstance() {
    assertIsInstance(BoolType.getInstance(), BOOLEAN_DATA);
    assertIsNotInstance(
        BoolType.getInstance(),
        NULL_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);
  }

  @Test
  public void testIntTypeIsInstance() {
    assertIsInstance(IntType.getInstance(), INTEGER_DATA);
    assertIsNotInstance(
        IntType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);
  }

  @Test
  public void testFloatTypeIsInstance() {
    assertIsInstance(FloatType.getInstance(), FLOAT_DATA);
    assertIsNotInstance(
        FloatType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);
  }

  @Test
  public void testSanitizedTypeIsInstance() {
    assertIsInstance(SanitizedType.HtmlType.getInstance(), htmlData);
    assertIsNotInstance(
        SanitizedType.HtmlType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);

    assertIsInstance(SanitizedType.AttributesType.getInstance(), attributesData);
    assertIsNotInstance(
        SanitizedType.AttributesType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);

    assertIsInstance(SanitizedType.StyleType.getInstance(), cssData);
    assertIsNotInstance(
        SanitizedType.StyleType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        uriData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);

    assertIsInstance(SanitizedType.UriType.getInstance(), uriData);
    assertIsNotInstance(
        SanitizedType.UriType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        trustedResourceUriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);

    assertIsInstance(SanitizedType.TrustedResourceUriType.getInstance(), trustedResourceUriData);
    assertIsNotInstance(
        SanitizedType.TrustedResourceUriType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        jsData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);

    assertIsInstance(SanitizedType.JsType.getInstance(), jsData);
    assertIsNotInstance(
        SanitizedType.JsType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);
  }

  @Test
  public void testListTypeIsInstance() {
    ListType listOfString = ListType.of(StringType.getInstance());
    assertIsInstance(listOfString, LIST_DATA);
    assertIsNotInstance(
        listOfString,
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData,
        MAP_DATA,
        DICT_DATA);
  }

  @Test
  public void testMapTypeIsInstance() {
    LegacyObjectMapType mapOfStringToAny =
        LegacyObjectMapType.of(StringType.getInstance(), AnyType.getInstance());
    assertIsInstance(mapOfStringToAny, MAP_DATA, LIST_DATA);
    assertIsNotInstance(
        mapOfStringToAny,
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData);
  }

  @Test
  public void testRecordTypeIsInstance() {
    LegacyObjectMapType mapOfStringToAny =
        LegacyObjectMapType.of(StringType.getInstance(), AnyType.getInstance());
    assertIsInstance(mapOfStringToAny, MAP_DATA, DICT_DATA);
    assertIsNotInstance(
        mapOfStringToAny,
        NULL_DATA,
        BOOLEAN_DATA,
        STRING_DATA,
        INTEGER_DATA,
        FLOAT_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        jsData);
  }

  @Test
  public void testStringTypeIsInstance() {
    setUpSanitizedData();

    assertIsInstance(
        StringType.getInstance(),
        STRING_DATA,
        htmlData,
        attributesData,
        cssData,
        uriData,
        trustedResourceUriData,
        jsData);
    assertIsNotInstance(
        StringType.getInstance(),
        NULL_DATA,
        BOOLEAN_DATA,
        INTEGER_DATA,
        LIST_DATA,
        MAP_DATA,
        DICT_DATA);
  }

  private static void assertIsInstance(SoyType type, SoyValue... values) {
    for (SoyValue value : values) {
      assertWithMessage(
              "Expected value of type "
                  + value.getClass().getName()
                  + " to be an instance of Soy type "
                  + type)
          .that(TofuTypeChecks.isInstance(type, value, SourceLocation.UNKNOWN))
          .isTrue();
    }
  }

  private static void assertIsNotInstance(SoyType type, SoyValue... values) {
    for (SoyValue value : values) {
      assertWithMessage(
              "Expected value of type "
                  + value.getClass().getName()
                  + " to NOT be an instance of Soy type "
                  + type)
          .that(TofuTypeChecks.isInstance(type, value, null))
          .isFalse();
    }
  }
}
