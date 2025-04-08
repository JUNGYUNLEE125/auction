package com.socket.auction.utils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import com.socket.auction.dto.ReqSocketDto;

@Component
public class RedisUtil {

    @Autowired
    StringRedisTemplate redisTemplate;

    private Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    /* 
    입력정보 : 경매정보, 이력정보, 일반경매자동종료연장정보, 1분배치정보
    키유형 : 경매정보(예시 info:1), 이력정보(예시 history:1), 일반경매자동종료연장정보(예시 endInfo:1), 1분배치정보(예시 batch:1)
    데이터유형 : List, 데이터 중복허용됨
    */

    public String setSocketId(String service) {
        String result = null;

        switch(service) {
            case "jasonapp018":
                result = "simsale-";
            break;
            case "jasonapp014":
                result = "sale09-";
            break;
            default:
                result = "market09-";
        }

        return result;
    }
    
    public Long setList(ReqSocketDto reqSocketDto, String key, String infoValue) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + key;

        Long result = redisTemplate.opsForList().leftPush(nextKey, infoValue);

        logger.info("Redis setList Key : "+ nextKey);
        logger.info("Redis setList Value : "+ infoValue);
        logger.info("Redis setList Result : "+ result);

        return result;
    }

    // List 데이터 추출 
    public List<String> preList(ReqSocketDto reqSocketDto, String key, int start, int end) {
        String nextKey = key;

        List<String> result = redisTemplate.opsForList().range(nextKey, start, end);

        logger.info("Redis getList Key : "+ nextKey);
        logger.info("Redis getList Result : "+ result);

        return result;
    }

    // List 데이터 추출 
    public List<String> getList(ReqSocketDto reqSocketDto, String key, int start, int end) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + key;

        List<String> result = redisTemplate.opsForList().range(nextKey, start, end);

        logger.info("Redis getList Key : "+ nextKey);
        logger.info("Redis getList Result : "+ result);

        return result;
    }

    // List 데이터 추출 
    public String popList(ReqSocketDto reqSocketDto, String key) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + key;

        String result = redisTemplate.opsForList().leftPop(nextKey);

        logger.info("Redis popList Key : "+ nextKey);
        logger.info("Redis popList Result : "+ result);

        return result;
    }

    // List 갯수 추출
    public int getListSize(ReqSocketDto reqSocketDto, String key) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + key;

        int result = Long.valueOf(Optional.ofNullable(redisTemplate.opsForList().size(nextKey)).orElse(0L)).intValue();

        logger.info("Redis getListSize Key : "+ nextKey);
        logger.info("Redis getListSize Result : "+ result);

        return result;
    }

    /*
    입력정보 : 입찰정보
    키유형 : 입찰정보(예시 rank:1)
    데이터유형 : Sorted Set, 데이터 중복허용 안됨, 데이터중복시 업데이트
    */
    public void setRank(ReqSocketDto reqSocketDto, String rankKey, String rankValue, int rankScore) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + rankKey;

        redisTemplate.opsForZSet().add(nextKey, rankValue, rankScore);

        logger.info("Redis setRank Key : "+ nextKey);
        logger.info("Redis setRank Value : "+ rankValue);
        logger.info("Redis setRank Result : "+ rankScore);
    }

    // Sorted Set 데이터 추출
    public Set<TypedTuple<String>> preRank(ReqSocketDto reqSocketDto, String rankKey, int start, int end) {
        String nextKey = rankKey;

        Set<TypedTuple<String>> result = redisTemplate.opsForZSet().reverseRangeWithScores(nextKey, start, end);

        logger.info("Redis getRank Key : "+ nextKey);
        logger.info("Redis getRank Result : "+ result);

        return result;
    }

    // Sorted Set 데이터 추출
    public Set<TypedTuple<String>> getRank(ReqSocketDto reqSocketDto, String rankKey, int start, int end) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + rankKey;

        Set<TypedTuple<String>> result = redisTemplate.opsForZSet().reverseRangeWithScores(nextKey, start, end);

        logger.info("Redis getRank Key : "+ nextKey);
        logger.info("Redis getRank Result : "+ result);

        return result;
    }

    // Sorted Set 데이터 갯수 추출
    public int getRankSize(ReqSocketDto reqSocketDto, String key) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + key;

        int result = Long.valueOf(Optional.ofNullable(redisTemplate.opsForZSet().size(nextKey)).orElse(0L)).intValue();

        logger.info("Redis getRankSize Key : "+ nextKey);
        logger.info("Redis getRankSize Result : "+ result);

        return result;
    }

    /*
     * 입력정보 입찰자수
     * 키유형 : 입찰자수(예시 bidr:1)
     * 데이터유형 : Set, 데이터 중복허용 안됨
     */
    public void setBidr(ReqSocketDto reqSocketDto, String key, String value) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + key;

        redisTemplate.opsForSet().add(nextKey, value);

        logger.info("Redis setBidr Key : "+ nextKey);
        logger.info("Redis setBidr Value : "+ value);
    }

    // Set 데이터 추출
    public Set<String> preBidr(ReqSocketDto reqSocketDto, String key) {
        String nextKey = key;

        Set<String> result = redisTemplate.opsForSet().members(nextKey);

        logger.info("Redis getBidr Key : "+ nextKey);
        logger.info("Redis getBidr Result : "+ result);

        return result;
    }    

    // Set 데이터 추출
    public Set<String> getBidr(ReqSocketDto reqSocketDto, String key) {
        String preKey  = setSocketId(reqSocketDto.getService());
        String nextKey = preKey + key;

        Set<String> result = redisTemplate.opsForSet().members(nextKey);

        logger.info("Redis getBidr Key : "+ nextKey);
        logger.info("Redis getBidr Result : "+ result);

        return result;
    }    

    // Set String Data Type
    public void setStr(String key, String value) {
        redisTemplate.opsForValue().set(key, value);

        logger.info("Redis setStr Key : "+ key);
        logger.info("Redis setStr Value : "+ value);
    }

    // Get String Data Type
    public String getStr(String key) {
        String result = redisTemplate.opsForValue().get(key);       

        logger.info("Redis getStr Key : "+ key);
        logger.info("Redis getStr Result : "+ result);
        
        return result;
    }
}
