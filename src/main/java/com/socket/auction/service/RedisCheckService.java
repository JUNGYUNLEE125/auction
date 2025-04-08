package com.socket.auction.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.utils.RedisUtil;

@Service
public class RedisCheckService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    StringRedisTemplate redisTemplate;

    public List<String> getInfo(ReqSocketDto reqSocketDto) {
        String infoKey = "info:"+ reqSocketDto.getAct_sno();

        List<String> result = redisUtil.getList(reqSocketDto, infoKey, 0, -1);
        
        return result;
    }

    public Set<TypedTuple<String>> getRank(ReqSocketDto reqSocketDto) {
        String rankKey = "rank:"+ reqSocketDto.getAct_sno();
        Set<TypedTuple<String>> result = redisUtil.getRank(reqSocketDto, rankKey, 0, -1);
        return result;
    }

    public List<String> getHistory(ReqSocketDto reqSocketDto) {
        String historyKey = "history:"+ reqSocketDto.getAct_sno();
        List<String> result = redisUtil.getList(reqSocketDto, historyKey, 0, -1);
        return result;
    }

    public List<String> getEndInfo(ReqSocketDto reqSocketDto) {
        String endInfoKey = "endInfo:"+ reqSocketDto.getAct_sno();
        List<String> result = redisUtil.getList(reqSocketDto, endInfoKey, 0, -1);
        return result;
    }

    public List<String> getBatch(ReqSocketDto reqSocketDto) {
        String batchKey = "batch:"+ reqSocketDto.getAct_sno();
        List<String> result = redisUtil.getList(reqSocketDto, batchKey, 0, -1);
        return result;
    }

    public Set<String> getBidr(ReqSocketDto reqSocketDto) {
        String bidrKey = "bidr:"+ reqSocketDto.getAct_sno();
        Set<String> result = redisUtil.getBidr(reqSocketDto, bidrKey);
        return result;
    }    

    public String getStr(String strKey) {
        String result = redisUtil.getStr(strKey);
        return result;
    }  

    public Long setInfo(ReqSocketDto reqSocketDto) {
        Long   result  = 0L;
        String infoKey = "info:"+ reqSocketDto.getAct_sno();
        List<String> infoList = redisUtil.preList(reqSocketDto, infoKey, 0, -1);

        Collections.reverse(infoList);
        for(String info : infoList) {
            result = redisUtil.setList(reqSocketDto, infoKey, info);
        }

        return result;
    }

    public void setRank(ReqSocketDto reqSocketDto) {
        String rankKey = "rank:"+ reqSocketDto.getAct_sno();
        Set<TypedTuple<String>> rankList = redisUtil.preRank(reqSocketDto, rankKey, 0, -1);

        for(TypedTuple<String> rank : rankList){
            int rankScore = rank.getScore().intValue();
            redisUtil.setRank(reqSocketDto, rankKey, rank.getValue(), rankScore);
        }
    }

    public Long setHistory(ReqSocketDto reqSocketDto) {
        Long   result  = 0L;
        String historyKey = "history:"+reqSocketDto.getAct_sno();
        List<String> historyList = redisUtil.preList(reqSocketDto, historyKey, 0, -1);

        Collections.reverse(historyList);
        for(String history : historyList) {
            result = redisUtil.setList(reqSocketDto, historyKey, history);
        }

        return result;
    }

    public Long setEndInfo(ReqSocketDto reqSocketDto) {
        Long   result  = 0L;
        String endInfoKey = "endInfo:"+reqSocketDto.getAct_sno();
        List<String> endInfoList = redisUtil.preList(reqSocketDto, endInfoKey, 0, 0);

        for(String endInfo : endInfoList) {
            result = redisUtil.setList(reqSocketDto, endInfoKey, endInfo);
        }

        return result;
    }

    public Long setBatch(ReqSocketDto reqSocketDto) {
        Long   result  = 0L;
        String batchKey = "batch:"+reqSocketDto.getAct_sno();
        List<String> batchList = redisUtil.preList(reqSocketDto, batchKey, 0, 0);

        for(String batch : batchList) {
            result = redisUtil.setList(reqSocketDto, batchKey, batch);
        }

        return result;
    }

    public void setBidr(ReqSocketDto reqSocketDto) {
        String bidrKey = "bidr:"+reqSocketDto.getAct_sno();
        Set<String> bidrList = redisUtil.preBidr(reqSocketDto, bidrKey);

        for(String bidr : bidrList) {
            redisUtil.setBidr(reqSocketDto, bidrKey, bidr);
        }
    }   

    public void setStr(String strKey, String strValue) {
        redisUtil.setStr(strKey, strValue);
    }   
    
    public void delRedis(String key) {
        redisTemplate.delete(key);
        redisTemplate.delete(key);
        redisTemplate.delete(key);
        redisTemplate.delete(key);
        redisTemplate.delete(key);
        redisTemplate.delete(key);
    }

    public Long batchRedis(ReqSocketDto reqSocketDto, String batchValue) {
        Long   result  = 0L;
        String batchKey = "batch:"+reqSocketDto.getAct_sno();

        result = redisUtil.setList(reqSocketDto, batchKey, batchValue);

        return result;
    }

    public List<String> getInfo2(ReqSocketDto reqSocketDto) {
        String infoKey = "info:"+ reqSocketDto.getAct_sno();
        List<String> result = redisUtil.preList(reqSocketDto, infoKey, 0, -1);

        return result;
    }

    public Set<TypedTuple<String>> getRank2(ReqSocketDto reqSocketDto) {
        String rankKey = "rank:"+ reqSocketDto.getAct_sno();
        Set<TypedTuple<String>> result = redisUtil.preRank(reqSocketDto, rankKey, 0, -1);
        return result;
    }

    public List<String> getHistory2(ReqSocketDto reqSocketDto) {
        String historyKey = "history:"+reqSocketDto.getAct_sno();
        List<String> result = redisUtil.preList(reqSocketDto, historyKey, 0, -1);
        return result;
    }

    public List<String> getEndInfo2(ReqSocketDto reqSocketDto) {
        String endInfoKey = "endInfo:"+reqSocketDto.getAct_sno();
        List<String> result = redisUtil.preList(reqSocketDto, endInfoKey, 0, -1);
        return result;
    }

    public List<String> getBatch2(ReqSocketDto reqSocketDto) {
        String batchKey = "batch:"+reqSocketDto.getAct_sno();
        List<String> result = redisUtil.preList(reqSocketDto, batchKey, 0, -1);
        return result;
    }

    public Set<String> getBidr2(ReqSocketDto reqSocketDto) {
        String bidrKey = "bidr:"+reqSocketDto.getAct_sno();
        Set<String> result = redisUtil.preBidr(reqSocketDto, bidrKey);
        return result;
    }    

}
