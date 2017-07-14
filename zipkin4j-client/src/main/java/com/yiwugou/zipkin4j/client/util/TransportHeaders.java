package com.yiwugou.zipkin4j.client.util;

import lombok.Getter;

public enum TransportHeaders {

    TraceId("Yiwugou-Zipkin-TraceId"),

    SpanId("Yiwugou-Zipkin-SpanId"),

    ParentSpanId("Yiwugou-Zipkin-ParentSpanId"),

    Sampled("Yiwugou-Zipkin-Sampled");

    @Getter
    private final String name;

    private TransportHeaders(final String name) {
        this.name = name;
    }
}
