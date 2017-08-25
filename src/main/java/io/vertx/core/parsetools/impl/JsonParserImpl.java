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
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.async.NonBlockingJsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
  private Handler<Throwable> exceptionHandler;
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
          currentField = null;
          break;
        }
        case VALUE_TRUE: {
          Handler<Object> handler = valueHandler;
          if (handler != null) {
            handler.handle(Boolean.TRUE);
          }
          break;
        }
        case VALUE_FALSE: {
          Handler<Object> handler = valueHandler;
          if (handler != null) {
            handler.handle(Boolean.FALSE);
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
      throw new DecodeException(e.getMessage());
    }
  }

  @Override
  public void handle(Buffer event) {
    handle(event.getBytes());
  }

  @Override
  public void end() {
    handle((byte[]) null);
  }

  private void handle(byte[] bytes) {
    try {
      if (bytes != null) {
        parser.feedInput(bytes, 0, bytes.length);
      } else {
        parser.endOfInput();
      }
      while (true) {
        JsonToken token = parser.nextToken();
        if (token == null || token == JsonToken.NOT_AVAILABLE) {
          break;
        }
        tokenHandler.handle(token);
      }
    } catch (IOException e) {
      if (exceptionHandler != null) {
        exceptionHandler.handle(e);
      } else {
        throw new DecodeException(e.getMessage());
      }
    } catch (Exception e) {
      if (exceptionHandler != null) {
        exceptionHandler.handle(e);
      } else {
        throw e;
      }
    }
  }

  @Override
  public JsonParserImpl startObjectHandler(Handler<Void> handler) {
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
  public JsonParserImpl endObjectHandler(Handler<Void> handler) {
    leaveObjectHandler = handler;
    return this;
  }

  @Override
  public JsonParser startArrayHandler(Handler<Void> handler) {
    enterArrayHandler = handler;
    return this;
  }

  @Override
  public JsonParser endArrayHandler(Handler<Void> handler) {
    leaveArrayHandler = handler;
    return this;
  }

  @Override
  public String currentField() {
    return currentField;
  }

  private class BufferingHandler implements Handler<JsonToken> {

    Handler<TokenBuffer> handler;
    int depth;
    TokenBuffer buffer;

    @Override
    public void handle(JsonToken event) {
      try {
        switch (event) {
          case START_OBJECT:
          case START_ARRAY:
            if (depth++ == 0) {
              buffer = new TokenBuffer(Json.mapper, false);
            }
            if (event == JsonToken.START_OBJECT) {
              buffer.writeStartObject();
            } else {
              buffer.writeStartArray();
            }
            break;
          case FIELD_NAME:
            buffer.writeFieldName(parser.getCurrentName());
            break;
          case VALUE_NUMBER_INT:
            buffer.writeNumber(parser.getLongValue());
            break;
          case VALUE_STRING:
            buffer.writeString(parser.getText());
            break;
          case VALUE_TRUE:
            buffer.writeBoolean(true);
            break;
          case VALUE_FALSE:
            buffer.writeBoolean(false);
            break;
          case VALUE_NULL:
            buffer.writeNull();
            break;
          case END_OBJECT:
          case END_ARRAY:
            if (event == JsonToken.END_OBJECT) {
              buffer.writeEndObject();
            } else {
              buffer.writeEndArray();
            }
            if (--depth == 0) {
              tokenHandler = JsonParserImpl.this::handleToken;
              buffer.flush();
              handler.handle(buffer);
            }
            break;
          default:
            throw new UnsupportedOperationException("Not implemented " + event);
        }
      } catch (IOException e) {
        // Should not happen as we are buffering
        throw new VertxException(e);
      }
    }
  }

  @Override
  public JsonParser objectHandler(Handler<JsonObject> handler) {
    return objectHandler(handler, buffer -> {
      try {
        return new JsonObject(Json.mapper.readValue(buffer.asParser(), Map.class));
      } catch (Exception e) {
        throw new DecodeException("Failed to decode: " + e.getMessage());
      }
    });
  }

  @Override
  public <T> JsonParser objectHandler(Class<T> type, Handler<T> handler) {
    return objectHandler(handler, buffer -> {
      try {
        return Json.mapper.readValue(buffer.asParser(), type);
      } catch (Exception e) {
        throw new DecodeException("Failed to decode: " + e.getMessage());
      }
    });
  }

  @Override
  public <T> JsonParser objectHandler(TypeReference<T> type, Handler<T> handler) {
    return objectHandler(handler, buffer -> {
      try {
        return Json.mapper.readValue(buffer.asParser(), type);
      } catch (Exception e) {
        throw new DecodeException("Failed to decode: " + e.getMessage());
      }
    });
  }

  private <T> JsonParser objectHandler(Handler<T> handler, Function<TokenBuffer, T> mapper) {
    if (handler != null) {
      if (objectHandler == null) {
        objectHandler = new BufferingHandler();
      }
      objectHandler.handler = buffer -> {
        T obj;
        try {
          obj = mapper.apply(buffer);
        } catch (Exception e) {
          throw new DecodeException(e.getMessage());
        }
        handler.handle(obj);
      };
    } else {
      if (objectHandler != null) {
        objectHandler = null;
        tokenHandler = this::handleToken;
      }
    }
    return this;
  }

  @Override
  public JsonParser arrayHandler(Handler<JsonArray> handler) {
    return arrayHandler(handler, buffer -> {
      try {
        return new JsonArray(Json.mapper.readValue(buffer.asParser(), List.class));
      } catch (Exception e) {
        throw new DecodeException("Failed to decode: " + e.getMessage());
      }
    });
  }

  @Override
  public <T> JsonParser arrayHandler(Class<T> type, Handler<T> handler) {
    return arrayHandler(handler, buffer -> {
      try {
        return Json.mapper.readValue(buffer.asParser(), type);
      } catch (Exception e) {
        throw new DecodeException("Failed to decode: " + e.getMessage());
      }
    });
  }

  @Override
  public <T> JsonParser arrayHandler(TypeReference<T> type, Handler<T> handler) {
    return arrayHandler(handler, buffer -> {
      try {
        return Json.mapper.readValue(buffer.asParser(), type);
      } catch (Exception e) {
        throw new DecodeException("Failed to decode: " + e.getMessage());
      }
    });
  }

  private <T> JsonParser arrayHandler(Handler<T> handler, Function<TokenBuffer, T> mapper) {
    if (handler != null) {
      if (arrayHandler == null) {
        arrayHandler = new BufferingHandler();
      }
      arrayHandler.handler = buffer -> {
        T array;
        try {
          array = mapper.apply(buffer);
        } catch (Exception e) {
          throw new DecodeException(e.getMessage());
        }
        handler.handle(array);
      };
    } else {
      if (arrayHandler != null) {
        arrayHandler = null;
        tokenHandler = this::handleToken;
      }
    }
    return this;
  }

  @Override
  public JsonParser write(Buffer buffer) {
    handle(buffer);
    return this;
  }

  @Override
  public JsonParser exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }
}
