/*
 * Copyright (c) 2011-2014 The original author or authors
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
import io.vertx.core.impl.VertxInternal;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SchedulerInterceptorTest extends VertxTestBase {

  private void setInterceptor(BiFunction<Context, Runnable, Runnable> interceptor) {
    ((VertxInternal) vertx).taskInterceptor(interceptor);
  }

  @Test
  public void testRunOnContext() {
    AtomicInteger cnt = new AtomicInteger();
    setInterceptor((c, r) -> {
      cnt.incrementAndGet();
      return r;
    });

    vertx.runOnContext(v -> {
      assertEquals(1, cnt.get());
    });
    vertx.runOnContext(v -> {
      assertEquals(2, cnt.get());
      testComplete();
    });
    await();
  }

  @Test
  public void testExecuteBlocking() {
    AtomicInteger cnt = new AtomicInteger();
    AtomicReference<Context> ref = new AtomicReference<>();

    setInterceptor((c, r) -> {
      if (c == ref.get()) {
        cnt.incrementAndGet();
      }
      return r;
    });

    Context ctx = vertx.getOrCreateContext();
    ref.set(ctx);
    ctx.executeBlocking(f -> {
      assertEquals(1, cnt.get());
    }, true, null);

    ctx.executeBlocking(f -> {
      assertEquals(2, cnt.get());
      testComplete();
    }, true, null);

    await();
  }

  @Test
  public void testTimer() {
    AtomicInteger cnt = new AtomicInteger();
    AtomicReference<Context> ref = new AtomicReference<>();

    setInterceptor((c, r) -> {
      if (c == ref.get()) {
        cnt.incrementAndGet();
      }
      return r;
    });

    Context ctx = vertx.getOrCreateContext();
    ref.set(ctx);
    ctx.runOnContext(v -> {
      assertEquals(1, cnt.get());
      vertx.setTimer(1000, h -> {
        assertEquals(2, cnt.get());
        vertx.runOnContext(v2 -> {
          assertEquals(3, cnt.get());
          testComplete();
        });
      });
    });

    await();
  }

  @Test
  public void testRunPeriodic() {

    AtomicReference<Context> ref = new AtomicReference<>();

    AtomicInteger cnt = new AtomicInteger();
    setInterceptor((c, r) -> {
      if (c == ref.get()) {
        cnt.incrementAndGet();
      }
      return r;
    });

    Context ctx = vertx.getOrCreateContext();
    ref.set(ctx);
    ctx.runOnContext(v -> {
      assertEquals(1, cnt.get());
      vertx.setPeriodic(1000, h -> {
        assertEquals(2, cnt.get());
        vertx.cancelTimer(h);
        vertx.runOnContext(v2 -> {
          assertEquals(3, cnt.get());
          testComplete();
        });
      });
    });

    await();
  }
}
