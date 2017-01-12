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
package io.vertx.core.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.spi.concurrent.CompletableStage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class CompletableStageImpl<T> implements CompletableStage<T> {

  private final CompletableFuture<T> fut;

  public CompletableStageImpl() {
    this(new CompletableFuture<T>());
  }

  public CompletableStageImpl(CompletableFuture<T> fut) {
    this.fut = fut;
  }

  @Override
  public void handle(AsyncResult<T> event) {
    if (event.succeeded()) {
      fut.complete(event.result());
    } else {
      fut.completeExceptionally(event.cause());
    }
  }

  @Override
  public <U> CompletionStage<U> thenApply(Function<? super T, ? extends U> fn) {
    return new CompletableStageImpl<>(fut.thenApply(fn));
  }

  @Override
  public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
    return new CompletableStageImpl<>(fut.thenApplyAsync(fn));
  }

  @Override
  public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
    return new CompletableStageImpl<>(fut.thenApplyAsync(fn, executor));
  }

  @Override
  public CompletionStage<Void> thenAccept(Consumer<? super T> action) {
    return new CompletableStageImpl<>(fut.thenAccept(action));
  }

  @Override
  public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.thenAcceptAsync(action, exec));
    } else {
      return new CompletableStageImpl<>(fut.thenAcceptAsync(action));
    }
  }

  @Override
  public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
    return new CompletableStageImpl<>(fut.thenAcceptAsync(action, executor));
  }

  @Override
  public CompletionStage<Void> thenRun(Runnable action) {
    return new CompletableStageImpl<>(fut.thenRun(action));
  }

  @Override
  public CompletionStage<Void> thenRunAsync(Runnable action) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.thenRunAsync(action, exec));
    } else {
      return new CompletableStageImpl<>(fut.thenRunAsync(action));
    }
  }

  @Override
  public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor) {
    return new CompletableStageImpl<>(fut.thenRunAsync(action, executor));
  }

  @Override
  public <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
    return new CompletableStageImpl<>(fut.thenCombine(other, fn));
  }

  @Override
  public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.thenCombineAsync(other, fn, exec));
    } else {
      return new CompletableStageImpl<>(fut.thenCombineAsync(other, fn));
    }
  }

  @Override
  public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
    return new CompletableStageImpl<>(fut.thenCombineAsync(other, fn, executor));
  }

  @Override
  public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
    return new CompletableStageImpl<>(fut.thenAcceptBoth(other, action));
  }

  @Override
  public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.thenAcceptBothAsync(other, action, exec));
    } else {
      return new CompletableStageImpl<>(fut.thenAcceptBothAsync(other, action));
    }
  }

  @Override
  public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
    return new CompletableStageImpl<>(fut.thenAcceptBothAsync(other, action, executor));
  }

  @Override
  public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
    return new CompletableStageImpl<>(fut.runAfterBoth(other, action));
  }

  @Override
  public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.runAfterBothAsync(other, action, exec));
    } else {
      return new CompletableStageImpl<>(fut.runAfterBothAsync(other, action));
    }
  }

  @Override
  public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
    return new CompletableStageImpl<>(fut.runAfterBothAsync(other, action, executor));
  }

  @Override
  public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
    return new CompletableStageImpl<>(fut.applyToEither(other, fn));
  }

  @Override
  public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.applyToEitherAsync(other, fn, exec));
    } else {
      return new CompletableStageImpl<>(fut.applyToEitherAsync(other, fn));
    }
  }

  @Override
  public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
    return new CompletableStageImpl<>(fut.applyToEitherAsync(other, fn, executor));
  }

  @Override
  public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
    return new CompletableStageImpl<>(fut.acceptEither(other, action));
  }

  @Override
  public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.acceptEitherAsync(other, action, exec));
    } else {
      return new CompletableStageImpl<>(fut.acceptEitherAsync(other, action));
    }
  }

  @Override
  public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
    return new CompletableStageImpl<>(fut.acceptEitherAsync(other, action, executor));
  }

  @Override
  public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
    return new CompletableStageImpl<>(fut.runAfterEither(other, action));
  }

  @Override
  public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.runAfterEitherAsync(other, action, exec));
    } else {
      return new CompletableStageImpl<>(fut.runAfterEitherAsync(other, action));
    }
  }

  @Override
  public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
    return new CompletableStageImpl<>(fut.runAfterEitherAsync(other, action, executor));
  }

  @Override
  public <U> CompletionStage<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
    return new CompletableStageImpl<>(fut.thenCompose(fn));
  }

  @Override
  public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.thenComposeAsync(fn, exec));
    } else {
      return new CompletableStageImpl<>(fut.thenComposeAsync(fn));
    }
  }

  @Override
  public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
    return new CompletableStageImpl<>(fut.thenComposeAsync(fn, executor));
  }

  @Override
  public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
    return new CompletableStageImpl<>(fut.whenComplete(action));
  }

  @Override
  public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.whenCompleteAsync(action, exec));
    } else {
      return new CompletableStageImpl<>(fut.whenCompleteAsync(action));
    }
  }

  @Override
  public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
    return new CompletableStageImpl<>(fut.whenCompleteAsync(action, executor));
  }

  @Override
  public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
    return new CompletableStageImpl<>(fut.handle(fn));
  }

  @Override
  public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
    Executor exec = asyncExec();
    if (exec != null) {
      return new CompletableStageImpl<>(fut.handleAsync(fn, exec));
    } else {
      return new CompletableStageImpl<>(fut.handleAsync(fn));
    }
  }

  @Override
  public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
    return new CompletableStageImpl<>(fut.handleAsync(fn, executor));
  }

  @Override
  public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn) {
    return new CompletableStageImpl<>(fut.exceptionally(fn));
  }

  @Override
  public CompletableFuture<T> toCompletableFuture() {
    return fut;
  }

  private Executor asyncExec() {
    Context current = VertxImpl.context();
    boolean executeBlocking = Context.isOnWorkerThread() && current instanceof EventLoopContext;
    if (current != null) {
      return cmd -> {
        if (executeBlocking) {
          current.executeBlocking(v -> cmd.run(), ar -> {
          });
        } else {
          current.runOnContext(v -> cmd.run());
        }
      };
    } else {
      return null;
    }
  }
}
