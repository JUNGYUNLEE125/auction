package com.socket.auction.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.socket.auction.utils.RedisSubscriber;

/*
 * Socket 사용자를 Redis PubSub Channel에 저장 
 * get, set 로직으로 처리
 * 인자값은 Socket 접속시 파라미터 socket_id(경매번호, 회원아이디)로 이용
 */

@Component
public class RedisCache {

    @Autowired
    RedisMessageListenerContainer redisMessageListener;

    @Autowired
    RedisSubscriber redisSubscriber;

    // User Redis cache
    private static Map<String, ChannelTopic> redisChannels = new HashMap<>();;

    public void setRedisChannel(String socketId) {
        ChannelTopic channel = redisChannels.get(socketId);
        
        if(channel == null) {
            channel = new ChannelTopic(socketId);
            redisMessageListener.addMessageListener(redisSubscriber, channel);
            redisChannels.put(socketId, channel);
        }
    }

    public ChannelTopic getRedisChannel(String socketId){
        ChannelTopic channel = redisChannels.get(socketId);

        return channel;
    }
    
}
