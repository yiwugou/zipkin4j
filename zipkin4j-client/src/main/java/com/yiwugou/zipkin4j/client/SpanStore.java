package com.yiwugou.zipkin4j.client;

import zipkin.Span;

/**
 * 
 * <pre>
 * SpanStore
 * </pre>
 * 
 * @author zhanxiaoyong@yiwugou.com
 *
 * @since 2018年1月25日 下午4:17:57
 */
public interface SpanStore {
    public Span.Builder getSpan();

    public void setSpan(Span.Builder span);

    public void removeSpan();
}
