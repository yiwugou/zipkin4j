package com.yiwugou.zipkin4j.example.mysql;

import java.io.Closeable;
import java.io.IOException;

import com.yiwugou.zipkin4j.client.ZipkinClient;

public class ZipkinMySQLInterceptorManagementBean implements Closeable {

    public ZipkinMySQLInterceptorManagementBean(final ZipkinClient zipkinClinet) {
        this(zipkinClinet, null);
    }

    public ZipkinMySQLInterceptorManagementBean(final ZipkinClient zipkinClinet, String serviceName) {
        ZipkinMySQLInterceptor.setZeroZipkin(zipkinClinet);
        ZipkinMySQLInterceptor.setServiceName(serviceName);
    }

    @Override
    public void close() throws IOException {
        ZipkinMySQLInterceptor.setZeroZipkin(null);
    }
}
