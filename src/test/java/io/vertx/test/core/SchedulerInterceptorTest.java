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
import io.vertx.core.VertxOptions;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SchedulerInterceptorTest extends VertxTestBase {

  private volatile BiFunction<Context, Runnable, Runnable> interceptor;

  @Override
  protected VertxOptions getOptions() {
    return super.getOptions().setTaskInterceptor((ctx, task) -> {
      if (interceptor != null) {
        task = interceptor.apply(ctx, task);
      }
      return task;
    });
  }

  @Test
  public void testRunOnContext() {
    AtomicInteger cnt = new AtomicInteger();
    interceptor = (c, r) -> {
      cnt.incrementAndGet();
      return r;
    };

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
    Context ctx = vertx.getOrCreateContext();
    AtomicInteger cnt = new AtomicInteger();

    interceptor = (c, r) -> {
      if (c == ctx) {
        cnt.incrementAndGet();
      }
      return r;
    };

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
    Context ctx = vertx.getOrCreateContext();
    AtomicInteger cnt = new AtomicInteger();

    interceptor = (c, r) -> {
      if (c == ctx) {
        cnt.incrementAndGet();
      }
      return r;
    };

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
    Context ctx = vertx.getOrCreateContext();
    AtomicInteger cnt = new AtomicInteger();

    interceptor = (c, r) -> {
      if (c == ctx) {
        cnt.incrementAndGet();
      }
      return r;
    };

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
