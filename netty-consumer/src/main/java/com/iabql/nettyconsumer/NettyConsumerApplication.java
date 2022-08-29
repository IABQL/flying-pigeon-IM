package com.iabql.nettyconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class NettyConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyConsumerApplication.class, args);
    }

}
