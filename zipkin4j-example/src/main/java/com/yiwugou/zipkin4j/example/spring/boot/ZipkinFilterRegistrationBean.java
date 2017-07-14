package com.yiwugou.zipkin4j.example.spring.boot;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.yiwugou.zipkin4j.example.servlet.ZipkinServletFilter;

/**
 * 放到任意spring-boot可以扫到的包下面
 *
 * @author zhanxiaoyong@yiwugou.com
 *
 * @since 2017年7月14日 下午3:58:43
 */
public class ZipkinFilterRegistrationBean {
    @Bean
    public FilterRegistrationBean catFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        ZipkinServletFilter filter = new ZipkinServletFilter();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("cat-filter");
        registration.setOrder(1);
        return registration;
    }
}
