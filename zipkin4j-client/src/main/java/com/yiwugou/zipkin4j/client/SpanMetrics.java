package com.yiwugou.zipkin4j.client;

public interface SpanMetrics {
    void incrementAcceptedSpans(int quantity);

    void incrementDroppedSpans(int quantity);
}
