/*
 * Copyright (c) 2011-2017 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.impl.CollectionBuilder;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.ob.impl.MapBuilder;
import com.fasterxml.jackson.jr.ob.impl.TypeDetector;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Json {

  // Non-standard JSON but we allow C style comments in our JSON
  private static final JsonFactory f = new JsonFactory().enable(JsonParser.Feature.ALLOW_COMMENTS);

  private static final int DEFAULT_FEATURES = JSON.Feature.defaults()
    & ~JSON.Feature.USE_DEFERRED_MAPS.mask()
    | JSON.Feature.WRITE_NULL_PROPERTIES.mask()
    | JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY.mask();


  private static class VertxJSON extends JSON {

    private VertxJSON(int features) {
      super(features, f, null);
    }

    @Override
    protected TypeDetector _defaultTypeDetector(int features) {
      return new VertxTypeDetector(features);
    }
    @Override
    protected JSONWriter _defaultWriter(int features, TreeCodec tc, TypeDetector td) {
      return new VertxJSONWriter(features, td, tc);
    }
    @Override
    protected JSONReader _defaultReader(int features, TreeCodec tc, TypeDetector td) {
      return new VertxJSONReader(features, td, tc, CollectionBuilder.defaultImpl(), MapBuilder.defaultImpl());
    }
    @Override
    public JsonParser _parser(Object source) throws IOException, JSONObjectException {
      return super._parser(source);
    }
  }

  // Should be private
  public static final VertxJSON mapper = new VertxJSON(DEFAULT_FEATURES);
  static final VertxJSON prettyMapper = new VertxJSON(DEFAULT_FEATURES | JSON.Feature.PRETTY_PRINT_OUTPUT.mask());

  static {

/*

    SimpleModule module = new SimpleModule();
    // custom types
    module.addSerializer(JsonObject.class, new JsonObjectSerializer());
    module.addSerializer(JsonArray.class, new JsonArraySerializer());
    // he have 2 extensions: RFC-7493
    module.addSerializer(Instant.class, new InstantSerializer());
    module.addDeserializer(Instant.class, new InstantDeserializer());
    module.addSerializer(byte[].class, new ByteArraySerializer());
    module.addDeserializer(byte[].class, new ByteArrayDeserializer());
*/

    // mapper.registerModule(module);
    // prettyMapper.registerModule(module);
  }

  /**
   * Encode a POJO to JSON using the underlying Jackson mapper.
   *
   * @param obj a POJO
   * @return a String containing the JSON representation of the given POJO.
   * @throws EncodeException if a property cannot be encoded.
   */
  public static String encode(Object obj) throws EncodeException {
    try {
      return mapper.asString(obj);
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }

  /**
   * Encode a POJO to JSON using the underlying Jackson mapper.
   *
   * @param obj a POJO
   * @return a Buffer containing the JSON representation of the given POJO.
   * @throws EncodeException if a property cannot be encoded.
   */
  public static Buffer encodeToBuffer(Object obj) throws EncodeException {
    try {
      return Buffer.buffer(mapper.asBytes(obj));
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }

  /**
   * Encode a POJO to JSON with pretty indentation, using the underlying Jackson mapper.
   *
   * @param obj a POJO
   * @return a String containing the JSON representation of the given POJO.
   * @throws EncodeException if a property cannot be encoded.
   */
  public static String encodePrettily(Object obj) throws EncodeException {
    try {
      return prettyMapper.asString(obj);
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }

  /**
   * Decode a given JSON string to a POJO of the given class type.
   * @param str the JSON string.
   * @param clazz the class to map to.
   * @param <T> the generic type.
   * @return an instance of T
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static <T> T decodeValue(String str, Class<T> clazz) throws DecodeException {
    try {
      if (clazz == Map.class) {
        return (T) mapper.mapFrom(str);
      }
      return mapper.beanFrom(clazz, str);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage());
    }
  }

  /**
   * Decode a given JSON string.
   *
   * @param str the JSON string.
   *
   * @return a JSON element which can be a {@link JsonArray}, {@link JsonObject}, {@link String}, ...etc if the content is an array, object, string, ...etc
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static Object decodeValue(String str) throws DecodeException {
    try {
      Object value = mapper.anyFrom(str);
      if (value instanceof List) {
        List list = (List) value;
        return new JsonArray(list);
      } else if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;
        return new JsonObject(map);
      }
      return value;
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage());
    }
  }

  /**
   * Decode a given JSON string to a POJO of the given type.
   * @param str the JSON string.
   * @param type the type to map to.
   * @param <T> the generic type.
   * @return an instance of T
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static <T> T decodeValue(String str, TypeReference<T> type) throws DecodeException {
    try {
      JsonParser parser = mapper._parser(str);
      return mapper.asCodec().readValue(parser, type);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage(), e);
    }
  }

  /**
   * Decode a given JSON buffer.
   *
   * @param buf the JSON buffer.
   *
   * @return a JSON element which can be a {@link JsonArray}, {@link JsonObject}, {@link String}, ...etc if the buffer contains an array, object, string, ...etc
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static Object decodeValue(Buffer buf) throws DecodeException {
    try {
      Object value = mapper.anyFrom(new ByteBufInputStream(buf.getByteBuf()));
      if (value instanceof List) {
        List list = (List) value;
        return new JsonArray(list);
      } else if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;
        return new JsonObject(map);
      }
      return value;
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage());
    }
  }

  /**
   * Decode a given JSON buffer to a POJO of the given class type.
   * @param buf the JSON buffer.
   * @param type the type to map to.
   * @param <T> the generic type.
   * @return an instance of T
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static <T> T decodeValue(Buffer buf, TypeReference<T> type) throws DecodeException {
    try {
      JsonParser jsonParser = mapper._parser(new ByteBufInputStream(buf.getByteBuf()));
      return mapper.asCodec().readValue(jsonParser, type);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode:" + e.getMessage(), e);
    }
  }

  /**
   * Decode a given JSON buffer to a POJO of the given class type.
   * @param buf the JSON buffer.
   * @param clazz the class to map to.
   * @param <T> the generic type.
   * @return an instance of T
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static <T> T decodeValue(Buffer buf, Class<T> clazz) throws DecodeException {
    try {
      ByteBufInputStream src = new ByteBufInputStream(buf.getByteBuf());
      if (clazz == Map.class) {
        return (T) mapper.mapFrom(src);
      }
      return mapper.beanFrom(clazz, src);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode:" + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  static Object checkAndCopy(Object val, boolean copy) {
    if (val == null) {
      // OK
    } else if (val instanceof Number && !(val instanceof BigDecimal)) {
      // OK
    } else if (val instanceof Boolean) {
      // OK
    } else if (val instanceof String) {
      // OK
    } else if (val instanceof Character) {
      // OK
    } else if (val instanceof CharSequence) {
      val = val.toString();
    } else if (val instanceof JsonObject) {
      if (copy) {
        val = ((JsonObject) val).copy();
      }
    } else if (val instanceof JsonArray) {
      if (copy) {
        val = ((JsonArray) val).copy();
      }
    } else if (val instanceof Map) {
      if (copy) {
        val = (new JsonObject((Map)val)).copy();
      } else {
        val = new JsonObject((Map)val);
      }
    } else if (val instanceof List) {
      if (copy) {
        val = (new JsonArray((List)val)).copy();
      } else {
        val = new JsonArray((List)val);
      }
    } else if (val instanceof byte[]) {
      val = Base64.getEncoder().encodeToString((byte[])val);
    } else if (val instanceof Instant) {
      val = ISO_INSTANT.format((Instant) val);
    } else {
      throw new IllegalStateException("Illegal type in JsonObject: " + val.getClass());
    }
    return val;
  }

  static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
    Iterable<T> iterable = () -> sourceIterator;
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  static Instant readInstant(String text) {
    try {
      return Instant.from(ISO_INSTANT.parse(text));
    } catch (DateTimeException e) {
      throw new VertxException("Expected an ISO 8601 formatted date time" + text);
    }
  }
/*  private static class JsonObjectSerializer extends JsonSerializer<JsonObject> {
    @Override
    public void serialize(JsonObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeObject(value.getMap());
    }
  }

  private static class JsonArraySerializer extends JsonSerializer<JsonArray> {
    @Override
    public void serialize(JsonArray value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeObject(value.getList());
    }
  }

  private static class InstantSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(Instant value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeString(ISO_INSTANT.format(value));
    }
  }

  private static class InstantDeserializer extends JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String text = p.getText();
      try {
        return Instant.from(ISO_INSTANT.parse(text));
      } catch (DateTimeException e) {
        throw new InvalidFormatException(p, "Expected an ISO 8601 formatted date time", text, Instant.class);
      }
    }
  }

  private static class ByteArraySerializer extends JsonSerializer<byte[]> {

    @Override
    public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeString(Base64.getEncoder().encodeToString(value));
    }
  }

  private static class ByteArrayDeserializer extends JsonDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String text = p.getText();
      try {
        return Base64.getDecoder().decode(text);
      } catch (IllegalArgumentException e) {
        throw new InvalidFormatException(p, "Expected a base64 encoded byte array", text, Instant.class);
      }
    }
  }*/
}
