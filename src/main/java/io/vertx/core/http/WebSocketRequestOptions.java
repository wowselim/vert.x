/*
 * Copyright (c) 2011-2017 The original author or authors
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

package io.vertx.core.http;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * Options describing how a {@link HttpClient} will make {@link WebSocket} connections.
 *
 * @author Thomas Segismont
 */
@DataObject
public class WebSocketRequestOptions extends RequestOptions {

  /**
   * The default WebSocket headers = null.
   */
  public static final MultiMap DEFAULT_HEADERS = null;

  /**
   * The default WebSocket version = V13.
   */
  public static final WebsocketVersion DEFAULT_WEBSOCKET_VERSION = WebsocketVersion.V13;

  /**
   * The default WebSocket subprotocol = null.
   */
  public static final String DEFAULT_SUB_PROTOCOL = null;

  private MultiMap headers;
  private WebsocketVersion version;
  private String subProtocol;

  public WebSocketRequestOptions() {
    headers = DEFAULT_HEADERS;
    version = DEFAULT_WEBSOCKET_VERSION;
    subProtocol = DEFAULT_SUB_PROTOCOL;
  }

  public WebSocketRequestOptions(WebSocketRequestOptions other) {
    super(other);
    headers = other.headers == null ? null : new CaseInsensitiveHeaders().addAll(other.headers);
    version = other.version;
    subProtocol = other.subProtocol;
  }

  public WebSocketRequestOptions(JsonObject json) {
    super(json);
    JsonObject hdrs = json.getJsonObject("headers", null);
    if (hdrs != null) {
      headers = new CaseInsensitiveHeaders();
      for (Map.Entry<String, Object> entry : hdrs) {
        if (!(entry.getValue() instanceof String)) {
          throw new IllegalStateException("Invalid type for message header value " + entry.getValue().getClass());
        }
        headers.set(entry.getKey(), (String) entry.getValue());
      }
    }
    if (json.getValue("version") instanceof String) {
      version = WebsocketVersion.valueOf((String) json.getValue("version"));
    }
    subProtocol = json.getString("subProtocol");
  }

  /**
   * @return the WebSocket headers, may be null
   */
  public MultiMap getHeaders() {
    return headers;
  }

  /**
   * Set the WebSocket headers. Defaults to {@code null}.
   *
   * @param headers the WebSocket headers
   * @return a reference to this, so the API can be used fluently
   */
  public WebSocketRequestOptions setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }

  /**
   * @return the WebSocket version, may be null
   */
  public WebsocketVersion getVersion() {
    return version;
  }

  /**
   * Set the WebSocket version. Defaults to {@code V13}.
   *
   * @param version the WebSocket version
   * @return a reference to this, so the API can be used fluently
   */
  public WebSocketRequestOptions setVersion(WebsocketVersion version) {
    this.version = version;
    return this;
  }

  /**
   * @return the WebSocket subprotocol, may be null
   */
  public String getSubProtocol() {
    return subProtocol;
  }

  /**
   * Set the WebSocket subprotocol. Defaults to {@code null}.
   *
   * @param subProtocol the WebSocket subprotocol
   * @return a reference to this, so the API can be used fluently
   */
  public WebSocketRequestOptions setSubProtocol(String subProtocol) {
    this.subProtocol = subProtocol;
    return this;
  }

  @Override
  public String getHost() {
    return super.getHost();
  }

  @Override
  public WebSocketRequestOptions setHost(String host) {
    super.setHost(host);
    return this;
  }

  @Override
  public int getPort() {
    return super.getPort();
  }

  @Override
  public WebSocketRequestOptions setPort(int port) {
    super.setPort(port);
    return this;
  }

  @Override
  public boolean isSsl() {
    return super.isSsl();
  }

  @Override
  public WebSocketRequestOptions setSsl(boolean ssl) {
    super.setSsl(ssl);
    return this;
  }

  @Override
  public String getURI() {
    return super.getURI();
  }

  @Override
  public WebSocketRequestOptions setURI(String uri) {
    super.setURI(uri);
    return this;
  }
}
