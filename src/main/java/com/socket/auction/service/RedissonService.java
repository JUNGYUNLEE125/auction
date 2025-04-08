package com.socket.auction.service;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.socket.auction.controller.AuctionController;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.dto.ResSocketErrDto;
import com.socket.auction.utils.ValidationUtil;

@Service
public class RedissonService {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AuctionController auctionController;

    @Autowired
    AuctionService auctionService;

    @Autowired
    AuctionStusService auctionStusService;

    @Autowired
    ValidationUtil validationUtil;

    private Logger logger = LoggerFactory.getLogger(RedissonService.class);
    
    @Transactional
    public HashMap<String, Object> insertBid(ReqSocketDto reqSocketDto){
        int    actSno = reqSocketDto.getAct_sno();
        String preKey = setSocketId(reqSocketDto.getService());

        final String lockName =  preKey + "lock:" + actSno;
        final RLock  lock     = redissonClient.getLock(lockName);

        ResSocketDto resSocketDto = new ResSocketDto();
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            boolean isLocked = lock.tryLock(1, 3, TimeUnit.SECONDS);
            if(!isLocked) {
                logger.info("redisson insertBid rock fail : "+ reqSocketDto);
                // lock 획득 실패
                resSocketDto.setSuccess(0);
                ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

                resSocketErrDto.setCode("0018");
                resSocketErrDto.setReason("동일한 금액으로 입찰한 고객이 있습니다.");
                resSocketDto.setError(resSocketErrDto);         

                result.put("resSocketDto", resSocketDto);
                result.put("userMmbrInfm", null);
                        
                return result;
                // return resSocketDto;
            }

            logger.info("redisson insertBid rock success Start : "+ reqSocketDto);
            result = validationUtil.checkBid(reqSocketDto);            
            resSocketDto = (ResSocketDto) result.get("resSocketDto");

        } catch (InterruptedException e) {
            logger.error("redisson insertBid error : "+ e);
            logger.error("insertBid data"+ reqSocketDto);
        } finally {
            logger.info("redisson unlock check start: ");
            if(lock != null && lock.isLocked()) {
                lock.unlock();
                logger.info("redisson unlock check end: ");
            }
        }
        logger.info("redisson insertBid rock success End : "+ reqSocketDto);
        return result;
        // return resSocketDto;
    }
    
    @Transactional
    public String updateStatus(ReqSocketDto reqSocketDto, int actStusCd, String reqType){
        int    actSno = reqSocketDto.getAct_sno();
        String preKey = setSocketId(reqSocketDto.getService());

        final String lockName = preKey + "lock:" + actSno;
        final RLock  lock     = redissonClient.getLock(lockName);

        String messageStr = null;
        try {
            boolean isLocked = lock.tryLock(2, 3, TimeUnit.SECONDS);
            if(!isLocked) {
                logger.info("redisson updateStatus rock fail : "+ reqSocketDto);
                // lock 획득 실패
                messageStr = "경매 락실패, 재시도 해주세요.";
            }

            logger.info("redisson updateStatus rock success : "+ reqSocketDto);
            auctionController.updateStatus(reqSocketDto, actStusCd, reqType);        
        } catch (InterruptedException e) {
            logger.error("redisson updateStatus error : "+ e);
            logger.error("updateStatus data : "+ reqSocketDto);
        } finally {
            if(lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }

        return messageStr;
    }
    
    @Transactional
    public String insertActBid(ReqSocketDto reqSocketDto){

        final String lockName =  "lock:insertActBid";
        final RLock  lock     = redissonClient.getLock(lockName);

        String messageStr = "success";
        try {
            boolean isLocked = lock.tryLock(1, 2, TimeUnit.SECONDS);
            if(!isLocked) {
                logger.info("redisson insertActBid rock fail : "+ reqSocketDto);
                // lock 획득 실패
                messageStr = "경매 락실패, 재시도 해주세요.";
            }

            logger.info("redisson insertActBid rock success : "+ reqSocketDto);
            auctionService.insertActBid(reqSocketDto);
        } catch (InterruptedException e) {
            logger.error("redisson insertActBid error : "+ e);
            logger.error("insertActBid data : "+ reqSocketDto);
        } finally {
            if(lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }

        return messageStr;
    }
    
    @Transactional
    public String endAuction(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto, String reqType){
        int    actSno = reqSocketDto.getAct_sno();
        String preKey = setSocketId(reqSocketDto.getService());

        final String lockName =  preKey + "lock:" + actSno;
        final RLock  lock     = redissonClient.getLock(lockName);

        String messageStr = null;
        try {
            boolean isLocked = lock.tryLock(2, 3, TimeUnit.SECONDS);
            if(!isLocked) {
                // lock 획득 실패
                logger.info("redisson endAuction rock fail :"+ reqSocketDto);
                messageStr = "경매 락실패, 재시도 해주세요.";
            }

            logger.info("redisson endAuction rock success : "+ reqSocketDto);
            auctionStusService.endAuction(reqSocketDto, resSocketDto, reqType);     
        } catch (InterruptedException e) {
            logger.error("redisson endAuction error"+ e);
            logger.error("endAuction data"+ reqSocketDto);
        } finally {
            if(lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }

        return messageStr;
    }

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
}
