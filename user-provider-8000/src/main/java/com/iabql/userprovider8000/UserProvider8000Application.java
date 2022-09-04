package com.iabql.userprovider8000;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserProvider8000Application {

    public static void main(String[] args) {
        SpringApplication.run(UserProvider8000Application.class, args);
    }

}
