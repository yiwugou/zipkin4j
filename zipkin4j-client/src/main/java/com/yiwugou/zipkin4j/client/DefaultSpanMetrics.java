package com.yiwugou.zipkin4j.client;

/**
 * 
 * <pre>
 * DefaultSpanMetrics
 * </pre>
 * 
 * @author zhanxiaoyong@yiwugou.com
 *
 * @since 2018年1月25日 下午4:17:48
 */
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
