package com.yiwugou.zipkin4j.example.util;

public interface MethodDone<T> {
    T done() throws Throwable;
}
