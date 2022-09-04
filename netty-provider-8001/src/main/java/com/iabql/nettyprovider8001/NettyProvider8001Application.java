package com.iabql.nettyprovider8001;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

@SpringBootApplication
@Slf4j
public class NettyProvider8001Application {
    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }

    public static void main(String[] args) {
        SpringApplication.run(NettyProvider8001Application.class, args);
    }

    @PostConstruct
    public void init(){
        try {
            // 获取nacos服务
            NamingService namingService = NamingFactory.createNamingService("127.0.0.1:8848");
            // 将服务注册到nacos
            namingService.registerInstance("netty-server","127.0.0.1",8888);
        }catch (Exception e){
            log.error("服务注册失败");
        }
    }
}
