package com.lenis0012.pluginutils.task;

import org.bukkit.Bukkit;
import org.spigotmc.AsyncCatcher;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.FALSE;

public class Task<T> {
    private static TaskExecutor taskExecutor = TaskExecutor.BLOCKING;

    public static void setTaskExecutor(TaskExecutor taskExecutor) {
        Task.taskExecutor = taskExecutor;
    }

    private final CompletionStage<T> stage;
    private final Boolean sync;

    private Task(CompletionStage<T> stage, Boolean sync) {
        this.stage = stage;
        this.sync = sync;
    }

    public static <T> Task<T> ofFuture(CompletableFuture<T> future) {
        return new Task<>(future, null);
    }

    public static <T> Task<T> completed(T value) {
        return new Task<>(CompletableFuture.completedFuture(value), AsyncCatcher.enabled && Bukkit.isPrimaryThread() ? true : null);
    }

    public static <T> Task<T> failed(Throwable exception) {
        return new Task<>(CompletableFuture.failedFuture(exception), AsyncCatcher.enabled && Bukkit.isPrimaryThread() ? true : null);
    }

    public static <T> Task<T> supplyAsync(Supplier<T> supplier) {
        return new Task<>(CompletableFuture.supplyAsync(supplier, taskExecutor.async()), false);
    }

    public static Task<Void> runAsync(Runnable runnable) {
        return new Task<>(CompletableFuture.runAsync(runnable, taskExecutor.async()), false);
    }

    public static Task<Void> allOf(Task<?>... tasks) {
        CompletableFuture[] futures = Arrays.stream(tasks)
                .map(t -> t.stage.toCompletableFuture())
                .toArray(CompletableFuture[]::new);

        return new Task<Void>(CompletableFuture.allOf(futures), null);
    }

    public <N> Task<N> thenApplySync(Function<T, N> function) {
        return new Task<>(TRUE.equals(sync) ?
                stage.thenApply(function) :
                stage.thenApplyAsync(function, taskExecutor.sync()),
                true
        );
    }

    public <N> Task<N> thenApplyAsync(Function<T, N> function) {
        return new Task<>(FALSE.equals(sync) ?
                stage.thenApply(function) :
                stage.thenApplyAsync(function, taskExecutor.async()),
                false
        );
    }

    public Task<Void> thenRunSync(Runnable runnable) {
        return new Task<>(TRUE.equals(sync) ?
                stage.thenRun(runnable) :
                stage.thenRunAsync(runnable, taskExecutor.sync()),
                true
        );
    }

    public Task<Void> thenRunAsync(Runnable runnable) {
        return new Task<>(FALSE.equals(sync) ?
                stage.thenRun(runnable) :
                stage.thenRunAsync(runnable, taskExecutor.async()),
                false
        );
    }

    public Task<Void> thenRunSync(Consumer<T> consumer) {
        return new Task<>(TRUE.equals(sync) ?
                stage.thenAccept(consumer) :
                stage.thenAcceptAsync(consumer, taskExecutor.sync()),
                true
        );
    }

    public Task<Void> thenAcceptAsync(Consumer<T> consumer) {
        return new Task<>(FALSE.equals(sync) ?
                stage.thenAccept(consumer) :
                stage.thenAcceptAsync(consumer, taskExecutor.async()),
                false
        );
    }

    public <N> Task<N> thenComposeSync(Function<T, Task<N>> composer) {
        return new Task<>(TRUE.equals(sync) ?
                stage.thenCompose(composer.andThen(t -> t.stage)) :
                stage.thenComposeAsync(composer.andThen(t -> t.stage), taskExecutor.sync()),
                true
        );
    }

    public <N> Task<N> thenComposeAsync(Function<T, Task<N>> composer) {
        return new Task<>(FALSE.equals(sync) ?
                stage.thenCompose(composer.andThen(t -> t.stage)) :
                stage.thenComposeAsync(composer.andThen(t -> t.stage), taskExecutor.async()),
                false
        );
    }

    public Task<T> whenCompleteSync(BiConsumer<T, Throwable> resultHandler) {
        return new Task<>(TRUE.equals(sync) ?
                stage.whenComplete(resultHandler) :
                stage.whenCompleteAsync(resultHandler, taskExecutor.sync()),
                true
        );
    }

    public Task<T> whenCompleteAsync(BiConsumer<T, Throwable> resultHandler) {
        return new Task<>(FALSE.equals(sync) ?
                stage.whenComplete(resultHandler) :
                stage.whenCompleteAsync(resultHandler, taskExecutor.async()),
                false
        );
    }
}
