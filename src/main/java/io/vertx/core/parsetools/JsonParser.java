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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.impl.JsonParserImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface JsonParser extends Handler<Buffer> {

  static JsonParser newParser() {
    return new JsonParserImpl();
  }

  @Fluent
  JsonParser enterObjectHandler(Handler<Void> handler);

  @Fluent
  JsonParser leaveObjectHandler(Handler<Void> handler);

  @Fluent
  JsonParser enterArrayHandler(Handler<Void> handler);

  @Fluent
  JsonParser leaveArrayHandler(Handler<Void> handler);

  @Fluent
  JsonParser fieldHandler(Handler<String> handler);

  String currentField();

  @Fluent
  JsonParser valueHandler(Handler<Object> handler);

  @Fluent
  JsonParser objectHandler(Handler<JsonObject> handler);

  @Fluent
  JsonParser arrayHandler(Handler<JsonArray> handler);

}
