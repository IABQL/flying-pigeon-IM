package com.iabql.nettyconsumer.ruleRobin;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RandomRobinRule {

    public ServiceInstance getServiceInstance(List<ServiceInstance> instances) {
        if (instances == null) return null;
        int random = new Random().nextInt(100);
        int index = random % (instances.size());
        return instances.get(index);
    }
}
