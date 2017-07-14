package com.yiwugou.zipkin4j.client.collector;

import zipkin.Span;

public interface SpanCollector {
    public void collect(final Span span);
}
