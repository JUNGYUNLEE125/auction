package com.socket.auction.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.service.RedisCheckService;

@Controller
@RestController
public class RedisCheckController {
    @Autowired
    RedisCheckService redisCheckService;

    @RequestMapping("/rdis/getInfo")
    public List<String> getInfo(@RequestParam int actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(actSno);
        reqSocketDto.setService(service);

        List<String> result = redisCheckService.getInfo(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getRank")
    public Set<TypedTuple<String>> getRank(@RequestParam int actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(actSno);
        reqSocketDto.setService(service);
        
        Set<TypedTuple<String>> result = redisCheckService.getRank(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getHistory")
    public List<String> getHistory(@RequestParam int actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(actSno);
        reqSocketDto.setService(service);
        
        List<String> result = redisCheckService.getHistory(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getEndInfo")
    public List<String> getEndInfo(@RequestParam int actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(actSno);
        reqSocketDto.setService(service);
        
        List<String> result = redisCheckService.getEndInfo(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getBatch")
    public List<String> getBatch(@RequestParam int actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(actSno);
        reqSocketDto.setService(service);
        
        List<String> result = redisCheckService.getBatch(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getBidr")
    public Set<String> getBidr(@RequestParam int actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(actSno);
        reqSocketDto.setService(service);
        
        Set<String> result = redisCheckService.getBidr(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getStr")
    public String getStr(@RequestParam String strKey) {
        String result = redisCheckService.getStr(strKey);

        return result;
    }

    @RequestMapping("/rdis/batchRedis")
    public String batchRedis(@RequestParam int actSno, @RequestParam String batchValue, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(actSno);
        reqSocketDto.setService(service);

        redisCheckService.batchRedis(reqSocketDto, batchValue);

        return "result";
    } 

    @RequestMapping("/rdis/setRedis")
    public String setRedis(@RequestParam String actSno, @RequestParam String service) {        
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(Integer.parseInt(actSno));
        reqSocketDto.setService(service);

        redisCheckService.setInfo(reqSocketDto);
        redisCheckService.setRank(reqSocketDto);
        redisCheckService.setHistory(reqSocketDto);
        redisCheckService.setEndInfo(reqSocketDto);
        redisCheckService.setBatch(reqSocketDto);
        redisCheckService.setBidr(reqSocketDto);

        return "result";
    } 

    @RequestMapping("/rdis/setStr")
    public String setStr(@RequestParam String strKey, @RequestParam String strValue) {
        redisCheckService.setStr(strKey, strValue);
        
        return "result";
    } 

    @RequestMapping("/rdis/delRedis")
    public String testRedis(@RequestParam int actSno) {
        
        redisCheckService.delRedis("info:"+actSno);
        redisCheckService.delRedis("rank:"+actSno);
        redisCheckService.delRedis("history:"+actSno);
        redisCheckService.delRedis("endInfo:"+actSno);
        redisCheckService.delRedis("batch:"+actSno);
        redisCheckService.delRedis("bidr:"+actSno);

        return "success";
    }   

    @RequestMapping("/rdis/getInfo2")
    public List<String> getInfo2(@RequestParam String actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(Integer.parseInt(actSno));
        reqSocketDto.setService(service);

        List<String> result = redisCheckService.getInfo2(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getRank2")
    public Set<TypedTuple<String>> getRank2(@RequestParam String actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(Integer.parseInt(actSno));
        reqSocketDto.setService(service);

        Set<TypedTuple<String>> result = redisCheckService.getRank2(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getHistory2")
    public List<String> getHistory2(@RequestParam String actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(Integer.parseInt(actSno));
        reqSocketDto.setService(service);

        List<String> result = redisCheckService.getHistory2(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getEndInfo2")
    public List<String> getEndInfo2(@RequestParam String actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(Integer.parseInt(actSno));
        reqSocketDto.setService(service);

        List<String> result = redisCheckService.getEndInfo2(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getBatch2")
    public List<String> getBatch2(@RequestParam String actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(Integer.parseInt(actSno));
        reqSocketDto.setService(service);

        List<String> result = redisCheckService.getBatch2(reqSocketDto);

        return result;
    }

    @RequestMapping("/rdis/getBidr2")
    public Set<String> getBidr2(@RequestParam String actSno, @RequestParam String service) {
        ReqSocketDto reqSocketDto = new ReqSocketDto();
        reqSocketDto.setAct_sno(Integer.parseInt(actSno));
        reqSocketDto.setService(service);

        Set<String> result = redisCheckService.getBidr2(reqSocketDto);

        return result;
    }
}
