package com.yiwugou.zipkin4j.example.spring.aop;

import java.util.Map;

import com.yiwugou.zipkin4j.client.ZipkinClient;
import com.yiwugou.zipkin4j.client.util.InetAddressUtils;
import com.yiwugou.zipkin4j.client.util.TraceKeys;
import com.yiwugou.zipkin4j.example.util.CommonUtils;
import com.yiwugou.zipkin4j.example.util.MethodDone;

import lombok.Setter;
import zipkin.Endpoint;

public class ZipkinMethodAspect {
    @Setter
    private ZipkinClient zeroZipkin;
    @Setter
    private String serviceName;

    public <R> R methodAspect(MethodDone<R> method, String spanName, Map<String, String> keyValues) throws Throwable {
        int ipv4 = InetAddressUtils.localIpv4();
        Endpoint endpoint = Endpoint.builder().serviceName(this.serviceName).ipv4(ipv4).build();
        this.zeroZipkin.startSpan(spanName);
        this.zeroZipkin.sendAnnotation(TraceKeys.CLIENT_SEND, endpoint);
        if (keyValues != null) {
            for (Map.Entry<String, String> keyValue : keyValues.entrySet()) {
                this.zeroZipkin.sendBinaryAnnotation(keyValue.getKey(), keyValue.getValue(), endpoint);
            }
        }
        try {
            R r = method.done();
            this.methodFinally(null, endpoint);
            return r;
        } catch (Throwable e) {
            this.methodFinally(e, endpoint);
            throw e;
        }
    }

    public <R> R methodAspect(MethodDone<R> method, String spanName) throws Throwable {
        return this.methodAspect(method, spanName, null);
    }

    private void methodFinally(Throwable e, Endpoint endpoint) {
        this.zeroZipkin.sendAnnotation(TraceKeys.CLIENT_RECV, endpoint);
        if (e != null) {
            this.zeroZipkin.sendBinaryAnnotation(this.serviceName + TraceKeys.SUFFIX_ERROR,
                    CommonUtils.errorToString(e), endpoint);
            this.zeroZipkin.sendBinaryAnnotation(this.serviceName + TraceKeys.SUFFIX_BACK, TraceKeys.RESULT_FAILED,
                    endpoint);
        } else {
            this.zeroZipkin.sendBinaryAnnotation(this.serviceName + TraceKeys.SUFFIX_BACK, TraceKeys.RESULT_SUCCESS,
                    endpoint);
        }
        this.zeroZipkin.finishSpan();
    }
}
