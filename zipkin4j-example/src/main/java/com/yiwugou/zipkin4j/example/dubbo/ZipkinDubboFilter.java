package com.yiwugou.zipkin4j.example.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.yiwugou.zipkin4j.client.ZipkinClient;
import com.yiwugou.zipkin4j.client.util.IdConversion;
import com.yiwugou.zipkin4j.client.util.InetAddressUtils;
import com.yiwugou.zipkin4j.client.util.TraceKeys;
import com.yiwugou.zipkin4j.client.util.TransportHeaders;
import com.yiwugou.zipkin4j.example.util.CommonUtils;

import lombok.Setter;
import zipkin.Endpoint;
import zipkin.Span;

/**
 * 
 * <pre>
 * ZipkinDubboFilter
 * </pre>
 * 
 * @author zhanxiaoyong@yiwugou.com
 *
 * @since 2018年1月25日 下午4:19:12
 */
@Activate(group = { Constants.PROVIDER, Constants.CONSUMER })
public class ZipkinDubboFilter implements Filter {
    @Setter
    private ZipkinClient zipkinClient = null;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext context = RpcContext.getContext();
        String host = context.getRemoteAddressString();
        int port = context.getRemotePort();
        String serviceInterface = context.getUrl().getServiceInterface();
        String methodName = invocation.getMethodName();
        Object[] args = invocation.getArguments();
        String methodAndArgs = CommonUtils.methodAndArgs(methodName, args);
        boolean isConsumer = context.isConsumerSide();
        boolean isProvider = context.isProviderSide();
        RpcInvocation rpcInvocation = (RpcInvocation) invocation;
        Endpoint endpoint = Endpoint.builder().serviceName("dubbo.consumer").port(port)
                .ipv4(InetAddressUtils.ipToInt(host)).build();
        if (isConsumer) {// 消费者时候 发出请求 clientSend clientResive
            Span span = this.zipkinClient.startSpan(serviceInterface);
            this.zipkinClient.sendAnnotation(TraceKeys.CLIENT_SEND, endpoint);
            this.zipkinClient.sendBinaryAnnotation("dubbo.consumer.url", context.getUrl().toServiceString(), endpoint);
            this.zipkinClient.sendBinaryAnnotation("dubbo.consumer.method", methodAndArgs, endpoint);
            rpcInvocation.setAttachment(TransportHeaders.Sampled.getName(), "1");
            rpcInvocation.setAttachment(TransportHeaders.TraceId.getName(), IdConversion.convertToString(span.traceId));
            rpcInvocation.setAttachment(TransportHeaders.SpanId.getName(), IdConversion.convertToString(span.id));
            if (span.parentId != null) {
                rpcInvocation.setAttachment(TransportHeaders.ParentSpanId.getName(),
                        IdConversion.convertToString(span.parentId));
            }

        } else if (isProvider) {
            final String parentId = invocation.getAttachment(TransportHeaders.ParentSpanId.getName());
            final String traceId = invocation.getAttachment(TransportHeaders.TraceId.getName());
            final String id = invocation.getAttachment(TransportHeaders.SpanId.getName());
            if (traceId == null) {
                this.zipkinClient.startSpan(serviceInterface);
            } else {
                this.zipkinClient.startSpan(IdConversion.convertToLong(id), IdConversion.convertToLong(traceId),
                        parentId == null ? null : IdConversion.convertToLong(parentId), serviceInterface);
            }
            this.zipkinClient.sendAnnotation(TraceKeys.SERVER_RECV, endpoint);
            this.zipkinClient.sendBinaryAnnotation("dubbo.provider.url", context.getUrl().toServiceString(), endpoint);
            this.zipkinClient.sendBinaryAnnotation("dubbo.provider.method", methodAndArgs, endpoint);
        }
        try {
            Result result = invoker.invoke(rpcInvocation);
            this.finallydo(isConsumer, isProvider, result.getException(), endpoint);
            return result;
        } catch (Exception e) {
            this.finallydo(isConsumer, isProvider, e, endpoint);
            throw e;
        }
    }

    private void finallydo(boolean isConsumer, boolean isProvider, Throwable e, Endpoint endpoint) {
        if (isConsumer) {
            this.zipkinClient.sendAnnotation(TraceKeys.CLIENT_RECV, endpoint);
            if (e != null) {
                this.zipkinClient.sendBinaryAnnotation("dubbo.consumer.error", CommonUtils.errorToString(e), endpoint);
                this.zipkinClient.sendBinaryAnnotation("dubbo.consumer.back", "failed", endpoint);
            } else {
                this.zipkinClient.sendBinaryAnnotation("dubbo.consumer.back", "success", endpoint);
            }
        } else if (isProvider) {
            this.zipkinClient.sendAnnotation(TraceKeys.SERVER_SEND, endpoint);
            if (e != null) {
                this.zipkinClient.sendBinaryAnnotation("dubbo.provider.error", CommonUtils.errorToString(e), endpoint);
                this.zipkinClient.sendBinaryAnnotation("dubbo.provider.back", "failed", endpoint);
            } else {
                this.zipkinClient.sendBinaryAnnotation("dubbo.provider.back", "success", endpoint);
            }
        }
        this.zipkinClient.finishSpan();
    }
}
