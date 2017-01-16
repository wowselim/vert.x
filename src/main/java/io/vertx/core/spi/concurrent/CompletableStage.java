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
package io.vertx.core.spi.concurrent;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.concurrent.CompletionStage;

/**
 * A completion stage completed with its {@link #handle(Object)} method.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface CompletableStage<T> extends CompletionStage<T>, Handler<AsyncResult<T>> {

  /**
   * @return an uncompleted instance
   */
  static <T> CompletableStage<T> create() {
    return ConcurrentFactory.instance.completableStage();
  }

}
