package com.socket.auction.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.service.AuctionService;

@Component
public class RedisSubscriber implements MessageListener {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    AuctionService auctionService;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String poSno = redisTemplate.getStringSerializer().deserialize(message.getChannel()).toString();
            String value = redisTemplate.getStringSerializer().deserialize(message.getBody()).toString();

            String[] actSno = poSno.split(":");

            final String REGEX = "[0-9]+";		
		
            if(actSno[1].matches(REGEX)) {
                ResSocketDto resSocketDto = objectMapper.readValue(value, ResSocketDto.class);
            
                if("02".equals(resSocketDto.getData().getAct_info().getAct_type_cd())) {
                    auctionService.sendEvent(poSno, "live", value);
                } else {
                    auctionService.sendEvent(poSno, "normal", value);
                }
            }else {
                auctionService.sendEvent(poSno, "my", value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}