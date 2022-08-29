package com.iabql.nettyprovider8001.netty;

import com.iabql.nettyprovider8001.SpringUtil;
import com.iabql.nettyprovider8001.redis.RedisUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户id 和channel 的关联关系处理
 */
@Component
@Slf4j
public class UserChanelRel {

    private static Map<String, Channel> manage = new ConcurrentHashMap<>();

    // 放入user对应的channel
    public static void put(String senderId,Channel channel){
        manage.put(senderId,channel);
    }

    // 获取user对应的channel
    public static Channel get(String senderId){
        return manage.get(senderId);
    }

    // 当socket断开，移除user对应的channel
    // 获取user对应的channel
    public static void remove(Channel channel){
        String senderId = "";
        for (String key : manage.keySet()) {
            if (manage.get(key) == channel){
                senderId = key;
                break;
            }
        }
        manage.remove(senderId);
        RedisUtils redisUtils = (RedisUtils) SpringUtil.getBean("redisUtils");
        redisUtils.remove(senderId);//并且移除redis缓存
    }

    @PreDestroy
    public void destroy() {
        log.info("正在释放redis资源");
        List<String> keys = new ArrayList<>();
        for (String key : manage.keySet()) {
            keys.add(key);
        }
        RedisUtils redisUtils = (RedisUtils) SpringUtil.getBean("redisUtils");
        redisUtils.remove(keys);
    }
}
