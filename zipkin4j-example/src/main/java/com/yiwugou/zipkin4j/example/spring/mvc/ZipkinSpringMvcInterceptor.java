package com.yiwugou.zipkin4j.example.spring.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.yiwugou.zipkin4j.client.ZipkinClient;
import com.yiwugou.zipkin4j.client.util.InetAddressUtils;
import com.yiwugou.zipkin4j.client.util.TraceKeys;

import zipkin.Endpoint;

/**
 *
 * ServletHandlerInterceptor
 *
 * @author zhanxiaoyong
 *
 * @since 2016年9月19日 下午4:53:39
 */
public class ZipkinSpringMvcInterceptor extends HandlerInterceptorAdapter {
    private static final String HTTP_ATTRIBUTE_ENDPOINT = ZipkinSpringMvcInterceptor.class.getName() + ".endpoint";

    private ZipkinClient zipkinClient;

    private String serviceName;

    public ZipkinSpringMvcInterceptor(ZipkinClient zipkinClient, String serviceName) {
        this.zipkinClient = zipkinClient;
        this.serviceName = serviceName;
    }

    public ZipkinSpringMvcInterceptor(ZipkinClient zeroZipkin) {
        this(zeroZipkin, "web");
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler) {
        Endpoint endpoint = Endpoint.builder().serviceName(this.serviceName)
                .ipv4(InetAddressUtils.ipToInt(request.getLocalAddr())).port(request.getLocalPort()).build();
        this.zipkinClient.startSpan(request.getMethod());
        this.zipkinClient.sendAnnotation(TraceKeys.SERVER_RECV, endpoint);
        request.setAttribute(HTTP_ATTRIBUTE_ENDPOINT, endpoint);
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler) {
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception ex) {
        final Endpoint endpoint = (Endpoint) request.getAttribute(HTTP_ATTRIBUTE_ENDPOINT);
        this.zipkinClient.sendAnnotation(TraceKeys.SERVER_SEND, endpoint);
        this.zipkinClient.sendBinaryAnnotation(TraceKeys.HTTP_STATUS_CODE, response.getStatus() + "", endpoint);
        this.zipkinClient.sendBinaryAnnotation(TraceKeys.HTTP_URL, request.getRequestURI(), endpoint);
        this.zipkinClient.finishSpan();
    }

}
