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
import io.vertx.core.Handler;
import io.vertx.core.spi.concurrent.CompletableStage;

import java.util.concurrent.CompletionStage;

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

  /**
   * Like {@link #get(Handler)}but returns a {@code CompletionStage} that will be
   * completed once the operation completes.
   */
  default CompletionStage<Long> get() {
    CompletableStage<Long> fut = CompletableStage.create();
    get(fut);
    return fut;
  }

  /**
   * Increment the counter atomically and return the new count
   *
   * @param resultHandler handler which will be passed the value
   */
  void incrementAndGet(Handler<AsyncResult<Long>> resultHandler);

  /**
   * Like {@link #incrementAndGet(Handler)}but returns a {@code CompletionStage} that will be
   * completed once the operation completes.
   */
  default CompletionStage<Long> incrementAndGet() {
    CompletableStage<Long> fut = CompletableStage.create();
    incrementAndGet(fut);
    return fut;
  }

  /**
   * Increment the counter atomically and return the value before the increment.
   *
   * @param resultHandler handler which will be passed the value
   */
  void getAndIncrement(Handler<AsyncResult<Long>> resultHandler);

  /**
   * Like {@link #getAndIncrement(Handler)}but returns a {@code CompletionStage} that will be
   * completed once the operation completes.
   */
  default CompletionStage<Long> getAndIncrement() {
    CompletableStage<Long> fut = CompletableStage.create();
    getAndIncrement(fut);
    return fut;
  }

  /**
   * Decrement the counter atomically and return the new count
   *
   * @param resultHandler handler which will be passed the value
   */
  void decrementAndGet(Handler<AsyncResult<Long>> resultHandler);

  /**
   * Like {@link #decrementAndGet(Handler)}but returns a {@code CompletionStage} that will be
   * completed once the operation completes.
   */
  default CompletionStage<Long> decrementAndGet() {
    CompletableStage<Long> fut = CompletableStage.create();
    decrementAndGet(fut);
    return fut;
  }

  /**
   * Add the value to the counter atomically and return the new count
   *
   * @param value  the value to add
   * @param resultHandler handler which will be passed the value
   */
  void addAndGet(long value, Handler<AsyncResult<Long>> resultHandler);

  /**
   * Like {@link #addAndGet(long, Handler)}but returns a {@code CompletionStage} that will be
   * completed once the operation completes.
   */
  default CompletionStage<Long> addAndGet(long value) {
    CompletableStage<Long> fut = CompletableStage.create();
    addAndGet(value, fut);
    return fut;
  }

  /**
   * Add the value to the counter atomically and return the value before the add
   *
   * @param value  the value to add
   * @param resultHandler handler which will be passed the value
   */
  void getAndAdd(long value, Handler<AsyncResult<Long>> resultHandler);

  /**
   * Like {@link #getAndAdd(long, Handler)}but returns a {@code CompletionStage} that will be
   * completed once the operation completes.
   */
  default CompletionStage<Long> getAndAdd(long value) {
    CompletableStage<Long> fut = CompletableStage.create();
    getAndAdd(value, fut);
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

  /**
   * Like {@link #compareAndSet(long, long, Handler)}but returns a {@code CompletionStage} that will be
   * completed once the operation completes.
   */
  default CompletionStage<Boolean> compareAndSet(long expected, long value) {
    CompletableStage<Boolean> fut = CompletableStage.create();
    compareAndSet(expected, value, fut);
    return fut;
  }
}
