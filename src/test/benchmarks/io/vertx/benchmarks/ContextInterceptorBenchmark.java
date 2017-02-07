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
package io.vertx.benchmarks;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.BenchmarkContext;
import io.vertx.core.impl.VertxInternal;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.function.BiFunction;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@State(Scope.Thread)
@Fork(jvmArgsAppend = { "-Dvertx.threadChecks=false", "-Dvertx.disableContextTimings=true", "-Dvertx.disableTCCL=true" })
public class ContextInterceptorBenchmark extends BenchmarkBase {

  @State(Scope.Thread)
  public static class RegularState {

    Vertx vertx;
    BenchmarkContext context;
    Handler<Void> task;

    @Setup
    public void setup(Blackhole hole) {
      vertx = Vertx.vertx();
      context = BenchmarkContext.create(vertx);
      task = v -> {
        hole.consume("the-string");
      };
    }
  }

  @State(Scope.Thread)
  public static class InterceptedState {

    Vertx vertx;
    BenchmarkContext context;
    Handler<Void> task;

    @Setup
    public void setup(Blackhole hole) {
      BiFunction<Context, Runnable, Runnable> interceptor = (ctx, task) -> {
        hole.consume(task);
        return task;
      };
      vertx = ((VertxInternal)Vertx.vertx()).addContextInterceptor(interceptor);
      context = BenchmarkContext.create(vertx);
      task = v -> {
        hole.consume("the-string");
      };
    }
  }

  @Benchmark
  public void baseline(RegularState state) {
    state.context.runDirect(state.task);
  }

  @Benchmark
  public void noInterceptor(RegularState state) {
    state.context.runOnContext(state.task);
  }

  @Benchmark
  public void oneInterceptor(InterceptedState state) {
    state.context.runOnContext(state.task);
  }
}
