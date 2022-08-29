package com.iabql.nettyconsumer.controller;

import com.iabql.nettyconsumer.ruleRobin.RandomRobinRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@RestController
public class NettyConsumerController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RandomRobinRule randomRobinRule;

    @Autowired
    private RestTemplate restTemplate;

    private List<ServiceInstance> instances = null;

    private static final String URL ="http://USER-PROVIDER";


    @RequestMapping("/getAllServer")
    public String getAllServer(){

        // 获取注册中心所有应用实例，例：springcloud-provider-dept
        List<String> services = discoveryClient.getServices();

        if (!services.isEmpty()){
            for (String service : services) {
                // 根据应用名称id，获取其所有的服务端信息
                if ("netty-provider".equals(service)){
                    // 获取netty的所有服务端实例
                    instances = discoveryClient.getInstances(service);
                }
            }
        }

        if (instances == null){
            return null;
        }

        // 负载均衡，随机策略,返回一个实例
        ServiceInstance serviceInstance = randomRobinRule.getServiceInstance(instances);
        // 获取其netty服务地址
        String nettyUrl = getIP(serviceInstance);
        return nettyUrl;
    }

    private String getIP(ServiceInstance instance) {
        URI uri = instance.getUri();// http://localhost:8001
        String host = uri.getHost();
        int port = uri.getPort() + 887;
        if("localhost".equals(host)){
            return "ws://10.8.241.66:" + port + "/ws";
        }
        return "ws://" + port + "/ws";
    }
}
