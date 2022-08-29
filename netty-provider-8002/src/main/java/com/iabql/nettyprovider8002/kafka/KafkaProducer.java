package com.iabql.nettyprovider8002.kafka;

import com.alibaba.fastjson.JSONObject;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class KafkaProducer {

    @Resource
    private KafkaTemplate kafkaTemplate;

    public void send(String topic, Object data) {
        kafkaTemplate.send(topic, JSONObject.toJSONString(data));
    }
}
