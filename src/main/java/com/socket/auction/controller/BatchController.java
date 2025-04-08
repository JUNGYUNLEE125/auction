package com.socket.auction.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.socket.auction.cache.RedisCache;
import com.socket.auction.cache.SocketCache;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.entity.ActEntity;
import com.socket.auction.service.AuctionService;
import com.socket.auction.service.AuctionStusService;
import com.socket.auction.service.RedisInfoService;
import com.socket.auction.service.RedissonService;
import com.socket.auction.utils.HttpUtils;
import com.socket.auction.utils.RepositoryUtil;

/*
 * 배치 프그그램 : 1분주기, checkAct 접속으로 endAuction, insertActBid 메소드 처리
 *   - endAuction : 진행중인 경매 종료시간 체크후 종료경매 처리
 *   - insertActBid : 진행중 경매 Redis history 입찰내용을 act_bit 테이블로 입력 처리
 */

@RestController
@RequestMapping("/batch")
public class BatchController {

    @Autowired
    RedisCache redisCache;

    @Autowired
    SocketCache socketCache;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedisInfoService redisInfoService;

    @Autowired
    AuctionStusService auctionStusService;

    @Autowired
    RedissonService redissonService;

    @Autowired
    AuctionService auctionService;

    @Autowired
    RepositoryUtil repositoryUtil;

    @Autowired
    HttpUtils httpUtils;

    private Logger logger = LoggerFactory.getLogger(BatchController.class);

	public String getSocketId(String service) {
		String result = null;
        
        switch(service) {
            case "jasonapp018":
                result = "simsale-";
                break;
            case "jasonapp014":
                result = "sale09-";
                break;
            case "jasonapp019":
                result = "market09-";
                break;
        }

		return result;
	}

    public boolean batchYn(int seq) {
        boolean result = false;

        switch(seq) {
            case 1:
                String btcTemp1 = redisTemplate.opsForValue().get("startBatchYn1");
                if("Y".equals(btcTemp1)) result = true;

                break;
            case 2:
                String btcTemp2 = redisTemplate.opsForValue().get("startBatchYn2");
                if("Y".equals(btcTemp2)) result = true;

                break;
            default:
        }
        logger.info("seq : "+ seq +", result : "+ result);

        return result;
    }

	private void sendMessage(String msg) {
        Gson gson = new Gson();

		HashMap<String, Object> postMap = new HashMap<String, Object>();
		postMap.put("serviceId", 4);
		postMap.put("botId", 7);
		postMap.put("messageCode", 1);
		postMap.put("message", msg);
		postMap.put("accountId", "sky_ljy@jasongroup.co.kr");
        
        httpUtils.sendPostJson("https://linebot.jasongroup.co.kr/sendMessage", gson.toJson(postMap));
	}

    @RequestMapping("/insertActBid")
    public HashMap<String, String> insertActBid(@RequestParam int seq, @RequestParam String service) {   
        boolean batchPassYn = batchYn(seq);

        HashMap<String, String> result = new HashMap<String, String>();
        if(batchPassYn) {
            ReqSocketDto reqSocketDto = new ReqSocketDto();
            reqSocketDto.setService(service);

            String message = redissonService.insertActBid(reqSocketDto);

            logger.info("Batch InsertActBid : "+ message);
            
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("result", "success");
        } else {
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("result", "pass");
        }

        return result;
    }    

    @RequestMapping("/endAuction")
    public HashMap<String, String> endAuction(@RequestParam int seq, @RequestParam String service) {    
        boolean batchPassYn = batchYn(seq);

        HashMap<String, String> result = new HashMap<String, String>();
        if(batchPassYn) {
            ReqSocketDto reqSocketDto = new ReqSocketDto();
            reqSocketDto.setService(service);
    
            String actSdtm = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))+":00";
            String actEdtm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))+":00";

            List<ActEntity> actEntityList = repositoryUtil.actByActTypeCdAndActStusCdAndActEdtmBetween(reqSocketDto, "01", "02", actSdtm, actEdtm);

            if(actEntityList.size() > 0){
                for(ActEntity actEntity : actEntityList){                      
                    reqSocketDto.setAct_sno(actEntity.getActSno());
                    redissonService.updateStatus(reqSocketDto, 5, "batch");

                    System.out.println(actEntity);
                }
            }
            
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("result", "success");
        } else {
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("result", "pass");
        }

        return result;
    }    

    @RequestMapping("/delRedis")
    public HashMap<String, String> delRedis(@RequestParam int seq, @RequestParam String service) {
        boolean batchPassYn = batchYn(seq);

        HashMap<String, String> result = new HashMap<String, String>();
        if(batchPassYn) {            
            ReqSocketDto reqSocketDto = new ReqSocketDto();
            reqSocketDto.setService(service);

            String actSdtm = LocalDateTime.now().minusHours(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"))+":00:00";
            String actEdtm = LocalDateTime.now().minusHours(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"))+":59:59";

            List<ActEntity> actEntityList = repositoryUtil.actByActStusCdAndActEdtmBetween(reqSocketDto, "03", actSdtm, actEdtm);
            if(actEntityList.size() > 0){
                String preKey  = getSocketId(service);

                for(ActEntity actEntity : actEntityList){
                    int actSno = actEntity.getActSno();
                    redisTemplate.delete(preKey + "info:" + actSno);
                    redisTemplate.delete(preKey + "rank:"+ actSno);
                    redisTemplate.delete(preKey + "history:"+ actSno);
                    redisTemplate.delete(preKey + "endInfo:"+ actSno);
                    redisTemplate.delete(preKey + "batch:"+ actSno);
                    redisTemplate.delete(preKey + "bidr:"+ actSno);
                }
            }

            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("result", "success");
        } else {
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("result", "pass");
        }

        return result;
    } 

    @RequestMapping("/logConcurrentUsers")
    public HashMap<String, String> logConcurrentUsers() {
        
        int socketUser = socketCache.logCountClient();

        logger.info("soccket concurrentUsers : " + socketUser);

        HashMap<String, String> result = new HashMap<String, String>();
        result.put("result", "success");

        return result;
    }   

    @RequestMapping("/testRedis")
    public HashMap<String, String> testRedis(@RequestParam int actSno, @RequestParam String service) {
        String preKey  = getSocketId(service);
        
        redisTemplate.delete(preKey + "info:" + actSno);
        redisTemplate.delete(preKey + "rank:" + actSno);
        redisTemplate.delete(preKey + "history:" + actSno);
        redisTemplate.delete(preKey + "endInfo:" + actSno);
        redisTemplate.delete(preKey + "batch:" + actSno);
        redisTemplate.delete(preKey + "bidr:" + actSno);

        HashMap<String, String> result = new HashMap<String, String>();
        result.put("result", "success");

        return result;
    }

    @RequestMapping("/batchHealthCheck")
    public HashMap<String, String> batchHealthCheck(@RequestParam int seq) throws InterruptedException {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("result", "success");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));            

        if(seq == 0) {
            return result;
        }

        LocalDateTime nowTime = LocalDateTime.now();
        String nowDate = nowTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String preTemp1 = redisTemplate.opsForValue().get("serverDateTime1");
        String preTemp2 = redisTemplate.opsForValue().get("serverDateTime2");

        String btcTemp1 = redisTemplate.opsForValue().get("startBatchYn1");
        String btcTemp2 = redisTemplate.opsForValue().get("startBatchYn2");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime preTime1 = LocalDateTime.parse(preTemp1, formatter);
        LocalDateTime preTime2 = LocalDateTime.parse(preTemp2, formatter);

        Duration duration1 = Duration.between(preTime1, nowTime);
        Duration duration2 = Duration.between(preTime2, nowTime);

        switch(seq) {
            case 1:
                if("N".equals(btcTemp1)) {
                    if(duration1.getSeconds() < 70) {
                        redisTemplate.opsForValue().set("startBatchYn1", "Y");
                        redisTemplate.opsForValue().set("startBatchYn2", "N");

                        sendMessage("Auction ServerName2 Crontab Start");
                        logger.info("batchHealthCheck ServerName2 Crontab Start : preTime("+ preTemp1 +"), nowTime("+ nowDate +"), duration("+ duration1.getSeconds() +"초)");
                    }
                }
                
                redisTemplate.opsForValue().set("serverDateTime1", nowDate);
                break;
            case 2:
                if("N".equals(btcTemp2)) {
                    if(duration1.getSeconds() > 300 && duration2.getSeconds() < 70) {
                        
                        redisTemplate.opsForValue().set("startBatchYn1", "N");
                        redisTemplate.opsForValue().set("startBatchYn2", "Y");
                        
                        sendMessage("Auction ServerName6 Crontab Start");
                        logger.info("batchHealthCheck ServerName6 Crontab Start : , duration1("+ duration1.getSeconds() +"초), duration2("+ duration2.getSeconds() +"초)");
                    }
                }
                
                redisTemplate.opsForValue().set("serverDateTime2", nowDate);
                break;
            default:
        }

        return result;
    }  
}
