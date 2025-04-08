package com.socket.auction.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
public class RedisPublisher {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public void publish(ChannelTopic topic, String resSocketDto) {
        if(topic != null){
            redisTemplate.convertAndSend(topic.getTopic(), resSocketDto);
        }
    }
}