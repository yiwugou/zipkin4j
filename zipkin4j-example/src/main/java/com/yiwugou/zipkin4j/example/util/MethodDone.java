package com.yiwugou.zipkin4j.example.util;

/**
 * 
 * <pre>
 * MethodDone
 * </pre>
 * 
 * @author zhanxiaoyong@yiwugou.com
 *
 * @since 2018年1月25日 下午4:20:30
 */
public interface MethodDone<T> {
    T done() throws Throwable;
}
