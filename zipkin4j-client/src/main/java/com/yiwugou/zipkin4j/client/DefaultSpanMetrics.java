package com.yiwugou.zipkin4j.client;

public class DefaultSpanMetrics implements SpanMetrics {
    private int accpected;
    private int dropped;

    @Override
    public void incrementAcceptedSpans(int quantity) {
        accpected = accpected + quantity;
    }

    @Override
    public void incrementDroppedSpans(int quantity) {
        dropped = dropped + quantity;
    }

}
