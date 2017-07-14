package com.yiwugou.zipkin4j.client.collector;

import java.io.IOException;
import java.util.List;

import zipkin.Span;

public class SystemSpanCollector extends AbstractSpanCollector {

    @Override
    public void sendSpans(byte[] json) throws IOException {
        List<Span> spans = super.bytesToList(json);
        for (Span span : spans) {
            System.err.println(span);
        }
    }

}
