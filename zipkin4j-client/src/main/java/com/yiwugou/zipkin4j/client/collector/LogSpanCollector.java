package com.yiwugou.zipkin4j.client.collector;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zipkin.Span;

public class LogSpanCollector extends AbstractSpanCollector {
    private static final Logger logger = LoggerFactory.getLogger(LogSpanCollector.class);

    @Override
    public void sendSpans(byte[] json) throws IOException {
        List<Span> spans = super.bytesToList(json);
        for (Span span : spans) {
            logger.info("{}", span);
        }
    }

}
