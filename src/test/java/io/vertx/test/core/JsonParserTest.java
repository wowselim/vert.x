/*
 * Copyright 2014 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.test.core;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JsonParserTest {

  @Test
  public void testParseEmptyObject() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.startObjectHandler(v -> {
      assertEquals(0, status.getAndIncrement());
    });
    parser.endObjectHandler(v -> {
      assertEquals(1, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("{}"));
    assertEquals(2, status.get());
  }

  @Test
  public void testParseEmptyArray() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.startArrayHandler(v -> {
      assertEquals(0, status.getAndIncrement());
    });
    parser.endArrayHandler(v -> {
      assertEquals(1, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("[]"));
    assertEquals(2, status.get());
  }

  @Test
  public void parseUnfinished() {
    Buffer data = Buffer.buffer("{\"un\":\"finished\"");
    try {
      JsonParser parser = JsonParser.newParser();
      parser.handle(data);
      parser.end();
      fail();
    } catch (DecodeException expected) {
    }
    JsonParser parser = JsonParser.newParser();
    List<Throwable> errors = new ArrayList<>();
    parser.exceptionHandler(errors::add);
    parser.handle(data);
    parser.end();
    assertEquals(1, errors.size());
  }

  @Test
  public void parseNumberFormatException() {
    Buffer data = Buffer.buffer(Long.MAX_VALUE + "0");
    try {
      JsonParser.newParser().valueHandler(val -> {}).write(data).end();
      fail();
    } catch (DecodeException expected) {
    }
    List<Throwable> errors = new ArrayList<>();
    JsonParser.newParser().exceptionHandler(errors::add).valueHandler(val -> {}).write(data).end();
    assertEquals(1, errors.size());
  }

  @Test
  public void testParseObjectValue() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.objectHandler(json -> {
      assertEquals(new JsonObject().put("number", 3).put("true", true).put("false", false).put("string", "s").putNull("null"), json);
      assertEquals(0, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("{\"number\":3,\"true\":true,\"false\":false,\"string\":\"s\",\"null\":null}"));
    assertEquals(1, status.get());
  }

  @Test
  public void testParseArrayValue() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.arrayHandler(json -> {
      assertEquals(new JsonArray().add(1).add(2).add(3), json);
      assertEquals(0, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("[1,2,3]"));
    assertEquals(1, status.get());
  }

  @Test
  public void testStringValue() {
    testValue("\"bar\"", "bar");
  }

  @Test
  public void testNullValue() {
    testValue("null", null);
  }

  @Test
  public void testLongValue() {
    testValue("567", 567L);
  }

  @Test
  public void testDoubleValue() {
    testValue("567.45", 567.45D);
  }

  private void testValue(String jsonValue, Object expected) {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.startObjectHandler(v -> {
      assertEquals(0, status.getAndIncrement());
    });
    parser.fieldHandler(s -> {
      assertEquals("foo", s);
      assertEquals("foo", parser.currentField());
      assertEquals(1, status.getAndIncrement());
    });
    parser.valueHandler(v -> {
      assertEquals(expected, v);
      assertEquals("foo", parser.currentField());
      assertEquals(2, status.getAndIncrement());
    });
    parser.endObjectHandler(v -> {
      assertEquals(3, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("{\"foo\":" + jsonValue + "}"));
    assertEquals(4, status.get());
  }

  @Test
  public void testParseObjectValueMembers() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.startObjectHandler(v -> {
      assertEquals(0, status.getAndIncrement());
      parser.objectHandler(json -> {
        switch (status.getAndIncrement()) {
          case 1:
            assertEquals("foo", parser.currentField());
            break;
          case 2:
            assertEquals("bar", parser.currentField());
            break;
          default:
            fail();
            break;
        }
      });
    });
    parser.endObjectHandler(v -> {
      assertEquals(3, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("{\"foo\":{},\"bar\":{}}"));
    assertEquals(4, status.get());
  }

  @Test
  public void testParseObjectValueList() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.startArrayHandler(v -> {
      assertEquals(0, status.getAndIncrement());
      parser.objectHandler(json -> {
        switch (status.getAndIncrement()) {
          case 1:
            assertEquals(new JsonObject().put("one", 1), json);
            break;
          case 2:
            assertEquals(new JsonObject().put("two", 2), json);
            break;
          case 3:
            assertEquals(new JsonObject().put("three", 3), json);
            break;
        }
      });
    });
    parser.endArrayHandler(v -> {
      assertEquals(4, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("[" +
      "{\"one\":1}," +
      "{\"two\":2}," +
      "{\"three\":3}" +
      "]"));
    assertEquals(5, status.get());
  }

  @Test
  public void testObjectHandlerScope() {
    JsonParser parser = JsonParser.newParser();
    List<JsonObject> objects = new ArrayList<>();
    parser.startObjectHandler(v -> {
      parser.objectHandler(objects::add);
    });
    AtomicInteger ends = new AtomicInteger();
    parser.endObjectHandler(v -> {
      ends.incrementAndGet();
    });
    parser.handle(Buffer.buffer("[" +
      "{\"one\":1}," +
      "{\"two\":2}," +
      "{\"three\":3}" +
      "]"));
    assertEquals(1, ends.get());
    assertEquals(Arrays.asList(new JsonObject().put("two", 2), new JsonObject().put("three", 3)), objects);
  }

  @Test
  public void testParseTopValues() {
    Map<String, Object> tests = new HashMap<>();
    tests.put("\"a-string\"", "a-string");
    tests.put("true", true);
    tests.put("false", false);
    tests.put("1234", 1234L);
    tests.put("" + Long.MAX_VALUE, Long.MAX_VALUE);
    tests.forEach((test, expected) -> {
      JsonParser parser = JsonParser.newParser();
      List<Object> values = new ArrayList<>();
      parser.valueHandler(values::add);
      parser.handle(Buffer.buffer(test));
      parser.end();
      assertEquals(Collections.singletonList(expected), values);
    });
  }

  @Test
  public void testObjectMapping() {
    JsonParser parser = JsonParser.newParser();
    List<Object> values = new ArrayList<>();
    parser.objectHandler(TheObject.class, values::add);
    parser.handle(new JsonObject().put("f", "the-value").toBuffer());
    assertEquals(Collections.singletonList(new TheObject("the-value")), values);
  }

  @Test
  public void testObjectMappingError() {
    List<Object> values = new ArrayList<>();
    List<Throwable> errors = new ArrayList<>();
    JsonParser.newParser().objectHandler(TheObject.class, values::add).exceptionHandler(errors::add).write(Buffer.buffer("{\"destination\":\"unknown\"}")).end();
    assertEquals(Collections.emptyList(), values);
    assertEquals(1, errors.size());
    try {
      JsonParser.newParser().objectHandler(TheObject.class, values::add).write(Buffer.buffer("{\"destination\":\"unknown\"}")).end();
      fail();
    } catch (DecodeException expected) {
    }
    assertEquals(Collections.emptyList(), values);
    assertEquals(1, errors.size());
  }

  @Test
  public void testObjectMappingWithTypeReference() {
    JsonParser parser = JsonParser.newParser();
    List<Object> values = new ArrayList<>();
    parser.objectHandler(new TypeReference<TheObject>() {}, values::add);
    parser.handle(new JsonObject().put("f", "the-value").toBuffer());
    assertEquals(Collections.singletonList(new TheObject("the-value")), values);
  }

  @Test
  public void testArrayMapping() {
    JsonParser parser = JsonParser.newParser();
    List<Object> values = new ArrayList<>();
    parser.arrayHandler(LinkedList.class, values::add);
    parser.handle(new JsonArray().add(0).add(1).add(2).toBuffer());
    assertEquals(Collections.singletonList(Arrays.asList(0L, 1L, 2L)), values);
    assertEquals(LinkedList.class, values.get(0).getClass());
  }

  @Test
  public void testArrayMappingError() {
    List<Object> values = new ArrayList<>();
    List<Throwable> errors = new ArrayList<>();
    JsonParser.newParser().arrayHandler(TheObject.class, values::add).exceptionHandler(errors::add).write(Buffer.buffer("[]")).end();
    assertEquals(Collections.emptyList(), values);
    assertEquals(1, errors.size());
    try {
      JsonParser.newParser().arrayHandler(TheObject.class, values::add).write(Buffer.buffer("[]")).end();
      fail();
    } catch (DecodeException expected) {
    }
    assertEquals(Collections.emptyList(), values);
    assertEquals(1, errors.size());
  }

  @Test
  public void testArrayMappingWithTypeReference() {
    JsonParser parser = JsonParser.newParser();
    List<Object> values = new ArrayList<>();
    parser.arrayHandler(new TypeReference<LinkedList<Long>>() {}, values::add);
    parser.handle(new JsonArray().add(0).add(1).add(2).toBuffer());
    assertEquals(Collections.singletonList(Arrays.asList(0L, 1L, 2L)), values);
    assertEquals(LinkedList.class, values.get(0).getClass());
  }

  public static class TheObject {

    private String f;

    public TheObject() {
    }

    public TheObject(String f) {
      this.f = f;
    }

    public void setF(String f) {
      this.f = f;
    }

    @Override
    public boolean equals(Object obj) {
      TheObject that = (TheObject) obj;
      return Objects.equals(f, that.f);
    }
  }
}
