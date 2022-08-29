package com.iabql.nettyconsumer.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfigBean {
    @Bean
    @LoadBalanced // 配置负载均衡,实现RestTemplate（Ribbon）
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
