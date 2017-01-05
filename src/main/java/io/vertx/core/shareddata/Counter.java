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

package io.vertx.core.shareddata;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * An asynchronous counter that can be used to across the cluster to maintain a consistent count.
 * <p>
 *
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Counter {

  /**
   * Get the current value of the counter
   *
   * @param resultHandler handler which will be passed the value
   */
  void get(Handler<AsyncResult<Long>> resultHandler);

  default Future<Long> get() {
    Future<Long> fut = Future.future();
    get(fut.completer());
    return fut;
  }

  /**
   * Increment the counter atomically and return the new count
   *
   * @param resultHandler handler which will be passed the value
   */
  void incrementAndGet(Handler<AsyncResult<Long>> resultHandler);

  default Future<Long> incrementAndGet() {
    Future<Long> fut = Future.future();
    incrementAndGet(fut.completer());
    return fut;
  }

  /**
   * Increment the counter atomically and return the value before the increment.
   *
   * @param resultHandler handler which will be passed the value
   */
  void getAndIncrement(Handler<AsyncResult<Long>> resultHandler);

  default Future<Long> getAndIncrement() {
    Future<Long> fut = Future.future();
    getAndIncrement(fut.completer());
    return fut;
  }

  /**
   * Decrement the counter atomically and return the new count
   *
   * @param resultHandler handler which will be passed the value
   */
  void decrementAndGet(Handler<AsyncResult<Long>> resultHandler);

  default Future<Long> decrementAndGet() {
    Future<Long> fut = Future.future();
    decrementAndGet(fut.completer());
    return fut;
  }

  /**
   * Add the value to the counter atomically and return the new count
   *
   * @param value  the value to add
   * @param resultHandler handler which will be passed the value
   */
  void addAndGet(long value, Handler<AsyncResult<Long>> resultHandler);

  default Future<Long> addAndGet(long value) {
    Future<Long> fut = Future.future();
    addAndGet(value, fut.completer());
    return fut;
  }

  /**
   * Add the value to the counter atomically and return the value before the add
   *
   * @param value  the value to add
   * @param resultHandler handler which will be passed the value
   */
  void getAndAdd(long value, Handler<AsyncResult<Long>> resultHandler);

  default Future<Long> getAndAdd(long value) {
    Future<Long> fut = Future.future();
    getAndAdd(value, fut.completer());
    return fut;
  }

  /**
   * Set the counter to the specified value only if the current value is the expectec value. This happens
   * atomically.
   *
   * @param expected  the expected value
   * @param value  the new value
   * @param resultHandler  the handler will be passed true on success
   */
  void compareAndSet(long expected, long value, Handler<AsyncResult<Boolean>> resultHandler);

  default Future<Boolean> compareAndSet(long expected, long value) {
    Future<Boolean> fut = Future.future();
    compareAndSet(expected, value, fut.completer());
    return fut;
  }
}
