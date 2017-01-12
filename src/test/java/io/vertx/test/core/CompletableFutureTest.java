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
package io.vertx.test.core;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.spi.concurrent.CompletableStage;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CompletableFutureTest extends VertxTestBase {

  @Test
  public void testAcceptCompleted() throws Exception {
    disableThreadChecks();
    Context ctx = vertx.getOrCreateContext();
    CompletableStage<Void> cf = CompletableStage.create();
    CountDownLatch latch = new CountDownLatch(1);
    vertx.runOnContext(v -> {
      cf.handle(Future.succeededFuture());
      latch.countDown();
    });
    awaitLatch(latch);
    cf.thenAccept(v -> {
      assertNull(Vertx.currentContext());
      testComplete();
    });
    await();
  }

  @Test
  public void testAcceptAsyncFromNonVertxThread() {
    disableThreadChecks();
    CompletableStage<Void> cf = CompletableStage.create();
    cf.thenAcceptAsync(v -> {
      assertNull(Vertx.currentContext());
      testComplete();
    });
    cf.handle(Future.succeededFuture());
    await();
  }

  @Test
  public void testAcceptAsyncFromEventLoopThread() throws Exception {
    testAcceptAsyncFromContextThread(vertx.getOrCreateContext());
  }

  @Test
  public void testAcceptAsyncFromWorkerThread() throws Exception {
    ContextImpl ctx = ((VertxImpl) vertx).createWorkerContext(false, null, null, new JsonObject(), Thread.currentThread().getContextClassLoader());
    testAcceptAsyncFromContextThread(ctx);
  }

  private void testAcceptAsyncFromContextThread(Context ctx) throws Exception {
    disableThreadChecks();
    CompletableStage<Void> cf = CompletableStage.create();
    CountDownLatch latch = new CountDownLatch(1);
    ctx.runOnContext(v -> {
      boolean workerThread = Context.isOnWorkerThread();
      CompletionStage<Void> ret = cf.thenAcceptAsync(v2 -> {
        assertSame(ctx, Vertx.currentContext());
        if (workerThread) {
          assertSame(true, Context.isOnWorkerThread());
        } else {
          assertSame(true, Context.isOnEventLoopThread());
        }
        testComplete();
      });
      assertTrue(ret instanceof CompletableStage);
      latch.countDown();
    });
    awaitLatch(latch);
    vertx.runOnContext(v -> {
      cf.handle(Future.succeededFuture());
    });
    await();
  }

  @Test
  public void testAcceptAsyncFromExecuteBlockingThread() throws Exception {
    disableThreadChecks();
    Context ctx = vertx.getOrCreateContext();
    CompletableStage<Void> cf = CompletableStage.create();
    CountDownLatch latch = new CountDownLatch(1);
    ctx.executeBlocking(fut -> {
      cf.thenAcceptAsync(v -> {
        assertSame(ctx, Vertx.currentContext());
        assertTrue(Context.isOnWorkerThread());
        testComplete();
      });
      fut.complete();
    }, ar -> latch.countDown());
    awaitLatch(latch);
    vertx.runOnContext(v -> {
      cf.handle(Future.succeededFuture());
    });
    await();
  }
}
