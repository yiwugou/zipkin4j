package com.yiwugou.zipkin4j.example.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yiwugou.zipkin4j.client.ZipkinClient;
import com.yiwugou.zipkin4j.client.util.InetAddressUtils;
import com.yiwugou.zipkin4j.client.util.TraceKeys;

import zipkin.Endpoint;

/**
 *
 * <pre>
 *
{@code
    <filter>
        <filter-name>cat-filter</filter-name>
        <filter-class>com.yiwugou.zipkin4j.example.servlet.ZipkinServletFilter</filter-class>
        <init-param>
            <param-name>zipkinHost</param-name>
            <param-value>http://127.0.0.1:9411</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>zipkin-filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
}
 *
 * </pre>
 *
 * ZipkinServletFilter
 *
 * @author zhanxiaoyong@yiwugou.com
 *
 * @since 2017年7月14日 下午3:30:29
 */
public class ZipkinServletFilter implements Filter {
    private ZipkinClient zipkinClient;
    private String serviceName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String zipkinHost = filterConfig.getInitParameter("zipkinHost");
        this.serviceName = filterConfig.getInitParameter("serviceName");
        this.zipkinClient = new ZipkinClient(zipkinHost);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Endpoint endpoint = Endpoint.builder().serviceName(this.serviceName)
                .ipv4(InetAddressUtils.ipToInt(request.getLocalAddr())).port(request.getLocalPort()).build();

        this.zipkinClient.startSpan(httpRequest.getMethod());
        this.zipkinClient.sendAnnotation(TraceKeys.SERVER_RECV, endpoint);
        try {
            chain.doFilter(request, response);
        } finally {
            this.zipkinClient.sendAnnotation(TraceKeys.SERVER_SEND, endpoint);
            this.zipkinClient.sendBinaryAnnotation(TraceKeys.HTTP_STATUS_CODE, httpResponse.getStatus() + "", endpoint);
            this.zipkinClient.sendBinaryAnnotation(TraceKeys.HTTP_URL, httpRequest.getRequestURI(), endpoint);
            this.zipkinClient.finishSpan();
        }
    }

    @Override
    public void destroy() {

    }

}
