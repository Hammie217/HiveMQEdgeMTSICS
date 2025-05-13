package com.hivemq.edge.adapters.helloworld;

import com.hivemq.adapter.sdk.api.data.DataPoint;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TestPollingOutput implements BatchPollingOutput {

    final @NotNull CompletableFuture<Boolean> outputFuture = new CompletableFuture<>();
    private final @NotNull Map<String, Object> dataPoints = new HashMap<>();
    private @Nullable String errorMessage = null;

    public TestPollingOutput() {
    }

    @Override
    public void addDataPoint(final @NotNull String tagName, final @NotNull Object tagValue) {
        dataPoints.put(tagName, tagValue);
    }

    @Override
    public void addDataPoint(final @NotNull DataPoint dataPoint) {
        // NOOP
    }

    @Override
    public void finish() {
        outputFuture.complete(true);
    }

    @Override
    public void fail(final @NotNull Throwable t, @Nullable final String errorMessage) {
        this.errorMessage = errorMessage;
        outputFuture.completeExceptionally(t);
    }

    @Override
    public void fail(@NotNull final String errorMessage) {
        this.errorMessage = errorMessage;
        outputFuture.completeExceptionally(new RuntimeException());
    }

    public @NotNull CompletableFuture<Boolean> getOutputFuture() {
        return outputFuture;
    }

    public @NotNull Map<String, Object> getDataPoints() {
        return dataPoints;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }
}