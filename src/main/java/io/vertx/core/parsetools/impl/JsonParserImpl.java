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
package io.vertx.core.parsetools.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.async.NonBlockingJsonParser;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JsonParserImpl implements JsonParser {

  private NonBlockingJsonParser parser;
  private BufferingHandler objectHandler;
  private BufferingHandler arrayHandler;
  private Handler<Void> enterObjectHandler;
  private Handler<Void> leaveObjectHandler;
  private Handler<Void> enterArrayHandler;
  private Handler<Void> leaveArrayHandler;
  private Handler<String> fieldHandler;
  private Handler<Object> valueHandler;
  private Handler<JsonToken> tokenHandler = this::handleToken;
  private String currentField;

  public JsonParserImpl() {
    JsonFactory factory = new JsonFactory();
    try {
      parser = (NonBlockingJsonParser) factory.createNonBlockingByteArrayParser();
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }

  private void handleToken(JsonToken token) {
    try {
      switch (token) {
        case START_OBJECT: {
          BufferingHandler handler = objectHandler;
          if (handler != null) {
            tokenHandler = handler;
            handler.handle(token);
          } else {
            Handler<Void> enterHandler = enterObjectHandler;
            if (enterHandler != null) {
              enterHandler.handle(null);
            }
          }
          break;
        }
        case START_ARRAY: {
          BufferingHandler handler = arrayHandler;
          if (handler != null) {
            tokenHandler = handler;
            handler.handle(token);
          } else {
            Handler<Void> enterHandler = enterArrayHandler;
            if (enterHandler != null) {
              enterHandler.handle(null);
            }
          }
          break;
        }
        case FIELD_NAME: {
          currentField = parser.getCurrentName();
          Handler<String> handler = fieldHandler;
          if (handler != null) {
            handler.handle(parser.getCurrentName());
          }
          break;
        }
        case VALUE_STRING: {
          Handler<Object> handler = valueHandler;
          if (handler != null) {
            handler.handle(parser.getText());
          }
          break;
        }
        case VALUE_NULL: {
          Handler<Object> handler = valueHandler;
          if (handler != null) {
            handler.handle(null);
          }
          break;
        }
        case VALUE_NUMBER_INT: {
          Handler<Object> handler = valueHandler;
          if (handler != null) {
            handler.handle(parser.getLongValue());
          }
          break;
        }
        case VALUE_NUMBER_FLOAT: {
          Handler<Object> handler = valueHandler;
          if (handler != null) {
            handler.handle(parser.getDoubleValue());
          }
          break;
        }
        case END_OBJECT: {
          Handler<Void> handler = leaveObjectHandler;
          if (handler != null) {
            handler.handle(null);
          }
          break;
        }
        case END_ARRAY: {
          Handler<Void> handler = leaveArrayHandler;
          if (handler != null) {
            handler.handle(null);
          }
          break;
        }
        default:
          throw new UnsupportedOperationException("Token " + token + " not implemented");
      }
    } catch (IOException e) {
      throw new VertxException(e);
    }
  }

  @Override
  public void handle(Buffer event) {
    try {
      byte[] bytes = event.getBytes();
      parser.feedInput(bytes, 0, bytes.length);
      while (true) {
        JsonToken token = parser.nextToken();
        if (token == JsonToken.NOT_AVAILABLE) {
          break;
        }
        tokenHandler.handle(token);
      }
    } catch (IOException e) {
      throw new VertxException(e);
    }
  }

  @Override
  public JsonParserImpl enterObjectHandler(Handler<Void> handler) {
    enterObjectHandler = handler;
    return this;
  }

  @Override
  public JsonParserImpl fieldHandler(Handler<String> handler) {
    fieldHandler = handler;
    return this;
  }

  @Override
  public JsonParser valueHandler(Handler<Object> handler) {
    valueHandler = handler;
    return this;
  }

  @Override
  public JsonParserImpl leaveObjectHandler(Handler<Void> handler) {
    leaveObjectHandler = handler;
    return this;
  }

  @Override
  public JsonParser enterArrayHandler(Handler<Void> handler) {
    enterArrayHandler = handler;
    return this;
  }

  @Override
  public JsonParser leaveArrayHandler(Handler<Void> handler) {
    leaveArrayHandler = handler;
    return this;
  }

  @Override
  public String currentField() {
    return currentField;
  }

  private class BufferingHandler implements Handler<JsonToken> {

    final Handler<String> handler;
    StringWriter buffer = new StringWriter();
    JsonGenerator gen;
    int depth;

    public BufferingHandler(Handler<String> handler) {
      this.handler = handler;
    }

    @Override
    public void handle(JsonToken event) {
      switch (event) {
        case START_OBJECT:
        case START_ARRAY: {
          if (depth++ == 0) {
            // Setup other handlers
            JsonFactory factory = new JsonFactory();
            try {
              gen = factory.createGenerator(buffer);
            } catch (IOException e) {
              throw new VertxException(e);
            }
          }
          try {
            if (event == JsonToken.START_OBJECT) {
              gen.writeStartObject();
            } else {
              gen.writeStartArray();
            }
          } catch (IOException e) {
            throw new VertxException(e);
          }
          break;
        }
        case FIELD_NAME: {
          try {
            gen.writeFieldName(parser.getCurrentName());
          } catch (IOException e) {
            throw new VertxException(e);
          }
          break;
        }
        case VALUE_NUMBER_INT: {
          try {
            gen.writeNumber(parser.getLongValue());
          } catch (IOException e) {
            throw new VertxException(e);
          }
          break;
        }
        case END_OBJECT:
        case END_ARRAY: {
          try {
            if (event == JsonToken.END_OBJECT) {
              gen.writeEndObject();
            } else {
              gen.writeEndArray();
            }
            if (--depth == 0) {
              tokenHandler = JsonParserImpl.this::handleToken;
              gen.flush();
              String s = buffer.toString();
              buffer.getBuffer().setLength(0);
              handler.handle(s);
            }
          } catch (IOException e) {
            throw new VertxException(e);
          }
          break;
        }
      }
    }
  }

  @Override
  public JsonParserImpl objectHandler(Handler<JsonObject> handler) {
    if (handler != null) {
      if (objectHandler != null) {
        throw new UnsupportedOperationException("what should we do ?");
      }
      objectHandler = new BufferingHandler(json -> handler.handle(new JsonObject(json)));
    } else {
      if (objectHandler != null) {
        objectHandler = null;
        tokenHandler = this::handleToken;
      }
    }
    return this;
  }

  @Override
  public JsonParserImpl arrayHandler(Handler<JsonArray> handler) {
    if (handler != null) {
      if (arrayHandler != null) {
        throw new UnsupportedOperationException("what should we do ?");
      }
      arrayHandler = new BufferingHandler(json -> handler.handle(new JsonArray(json)));
    } else {
      if (arrayHandler != null) {
        arrayHandler = null;
        tokenHandler = this::handleToken;
      }
    }
    return this;
  }
}
