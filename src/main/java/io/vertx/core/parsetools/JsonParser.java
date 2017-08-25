/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.core.parsetools;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.impl.JsonParserImpl;

/**
 * A parser class which allows to incrementally parse json elements and emit json parse events instead of parsing a json
 * element fully. This parser is convenient for parsing large json structures.
 * <p/>
 * The parser can also parse entire object or array when it is convenient, for instance a very large array
 * of small objects can be parsed efficiently by handling array <i>start</i>/<i>end</i> and <i>object</i>
 * events.
 * <p/>
 * Whenever the parser fails to parse or process the stream, the {@link #exceptionHandler(Handler)} is called with
 * the cause of the failure and the current handling stops. After such event, the parser should not handle data
 * anymore.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface JsonParser extends Handler<Buffer> {

  /**
   * Create a new {@code JsonParser} instance.
   */
  static JsonParser newParser() {
    return new JsonParserImpl();
  }

  /**
   * Handle a {@code Buffer}, pretty much like calling {@link #handle(Object)}.
   *
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser write(Buffer buffer);

  /**
   * End the stream, this must be called after all the json stream has been processed.
   */
  void end();

  /**
   * Set an handler to be called whenever the parser cannot parse or process the input.
   * <p/>
   * The handler won't handle errors thrown by handlers set on this parser such as {@link #startObjectHandler(Handler)}, etc...
   *
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser exceptionHandler(Handler<Throwable> handler);

  /**
   * Set an handler to be called when the parser detects the start of a json object.
   *
   * @param handler that will receive json object <i>start</i> events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser startObjectHandler(Handler<Void> handler);

  /**
   * Set an handler to be called when the parser detects the end of a json object.
   *
   * @param handler that will receive json object <i>end</i> events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser endObjectHandler(Handler<Void> handler);

  /**
   * Set an handler to be called when a json object has been fully parsed.
   * <p/>
   * Setting this handler overrides the <i>start</i>/<i>end</i> object handlers.
   *
   * @param handler that will receive json object events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser objectHandler(Handler<JsonObject> handler);

  /**
   * Like {@link #objectHandler(Handler)} but decode the value to a POJO of the given class type.
   *
   * @param type the class type
   * @param handler that will receive object events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  <T> JsonParser objectHandler(Class<T> type, Handler<T> handler);

  /**
   * Like {@link #objectHandler(Handler)} but decode the value to a POJO of the given type.
   *
   * @param type the type
   * @param handler that will receive object events
   * @return  a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <T> JsonParser objectHandler(TypeReference<T> type, Handler<T> handler);

  /**
   * Set an handler to be called when the parser detects the start of a json array.
   *
   * @param handler that will receive json array <i>start</i> events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser startArrayHandler(Handler<Void> handler);

  /**
   * Set an handler to be called when the parser detects the end of a json array.
   *
   * @param handler that will receive json array <i>end</i> events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser endArrayHandler(Handler<Void> handler);

  /**
   * Set an handler to be called when a json array has been fully parsed.
   * <p/>
   * Setting this handler overrides the <i>start</i>/<i>end</i> array handlers.
   *
   * @param handler that will receive json object events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser arrayHandler(Handler<JsonArray> handler);

  /**
   * Like {@link #arrayHandler(Handler)} but decode the value to a POJO of the given class type.
   *
   * @param type the class type
   * @param handler that will receive object events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  <T> JsonParser arrayHandler(Class<T> type, Handler<T> handler);

  /**
   * Like {@link #arrayHandler(Handler)} but decode the value to a POJO of the given type.
   *
   * @param type the type
   * @param handler that will receive object events
   * @return  a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <T> JsonParser arrayHandler(TypeReference<T> type, Handler<T> handler);

  /**
   * Set an handler to be called when the parser parses the member of a json object.
   *
   * @param handler that will receive json object <i>start</i> events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser fieldHandler(Handler<String> handler);

  /**
   * Returns the name of the parsed field, you can get it after the <i>value</i> handler has been called
   *
   * @return the name of the parsed field
   */
  String currentField();

  /**
   * Set an handler to be called when a value has been parsed, it can be a json object or a json array value.
   * <p/>
   * When the handler is called during the parsing of a json object, {@link #currentField()} returns the name
   * of the field.
   *
   * @param handler that will receive <i>value</i> events
   * @return  a reference to this, so the API can be used fluently
   */
  @Fluent
  JsonParser valueHandler(Handler<Object> handler);
}
