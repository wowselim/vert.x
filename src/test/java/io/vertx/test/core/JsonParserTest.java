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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JsonParserTest {

  @Test
  public void testParseEmptyObject() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.enterObjectHandler(v -> {
      assertEquals(0, status.getAndIncrement());
    });
    parser.leaveObjectHandler(v -> {
      assertEquals(1, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("{}"));
    assertEquals(2, status.get());
  }

  @Test
  public void testParseEmptyArray() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.enterArrayHandler(v -> {
      assertEquals(0, status.getAndIncrement());
    });
    parser.leaveArrayHandler(v -> {
      assertEquals(1, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("[]"));
    assertEquals(2, status.get());
  }

  @Test
  public void testParseObjectValue() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.objectHandler(json -> {
      assertEquals(new JsonObject().put("foo", 3), json);
      assertEquals(0, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("{\"foo\":3}"));
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
    parser.enterObjectHandler(v -> {
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
    parser.leaveObjectHandler(v -> {
      assertEquals(3, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("{\"foo\":" + jsonValue + "}"));
    assertEquals(4, status.get());
  }

  @Test
  public void testParseObjectValueMembers() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.enterObjectHandler(v -> {
      assertEquals(0, status.getAndIncrement());
      parser.objectHandler(json -> {
        assertEquals("foo", parser.currentField());
        assertEquals(1, status.getAndIncrement());
      });
    });
    parser.leaveObjectHandler(v -> {
      assertEquals(2, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("{\"foo\":{}}"));
    assertEquals(3, status.get());
  }


  @Test
  public void testParseObjectValueList() {
    JsonParser parser = JsonParser.newParser();
    AtomicInteger status = new AtomicInteger();
    parser.enterArrayHandler(v -> {
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
    parser.leaveArrayHandler(v -> {
      assertEquals(4, status.getAndIncrement());
    });
    parser.handle(Buffer.buffer("[" +
      "{\"one\":1}," +
      "{\"two\":2}," +
      "{\"three\":3}" +
      "]"));
    assertEquals(5, status.get());
  }
}
