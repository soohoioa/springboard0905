package com.project.board0905.common.config;

import com.project.board0905.common.util.RequestCorrelationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<RequestCorrelationFilter> requestCorrelationFilter() {
        FilterRegistrationBean<RequestCorrelationFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new RequestCorrelationFilter());
        reg.setOrder(1);
        return reg;
    }
}
