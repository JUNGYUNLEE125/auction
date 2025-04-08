package com.socket.auction.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socket.auction.cache.RedisCache;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResSocketChatInfoDto;
import com.socket.auction.dto.ResSocketDataDto;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.dto.ResSocketErrDto;
import com.socket.auction.dto.ResSocketListDto;
import com.socket.auction.dto.UserMmbrInfm;
import com.socket.auction.service.AuctionService;
import com.socket.auction.service.AuctionStusService;
import com.socket.auction.service.RedisInfoService;
import com.socket.auction.service.RedissonService;
import com.socket.auction.service.ResDtoService;
import com.socket.auction.utils.RedisPublisher;
import com.socket.auction.utils.ValidationUtil;

/* 데이터 인자 DTO : reqSocketDto로 전달
 * API Process Flow : ApiController > ApiService > AuctionController > ResDtoService > RedisInfoService > AuctionService, RedissonService
 * Socket Process Flow : SocketIoHandler > AuctionController > ResDtoService > RedisInfoService > AuctionService, RedissonService
 * 
 * ApiController : API 접속 분기
 * ApiService : Api 로직, 분기 > 로직별 경매 소켓 AuctionController 이동
 * 
 * SocketIoHandler : Socket 접속, 일반경매, 라이브경매, My(진행중 경매) 분기
 * 
 * AuctionController : 경매 Socket 상태별 분기
 * ResDtoService : Socket 통신에 사용할 데이터 DTO 생성, 수정
 * RedisInfoService : Socket 데이터에 사용할 Redis 데이터 삽입, 추출
 * AuctionService : 경매 로직 추출을 위한 메소드 집합
 * RedissonService : Redis 분삭 락 기능 - 입찰, 상태 변경시 사용
 */

@Controller
public class AuctionController {

    @Autowired
    RedisCache redisCache;

    @Autowired
    RedisPublisher redisPublisher;

    @Autowired
    ResDtoService resDtoService;

    @Autowired
    RedisInfoService redisInfoService;

    @Autowired
    AuctionService auctionService;

    @Autowired
    AuctionStusService auctionStusService;

    @Autowired
    RedissonService redissonService;

    @Autowired
    ValidationUtil validationUtil;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(AuctionController.class);

    public void initSocket(SocketIOClient client, ReqSocketDto reqSocketDto, String eventName, String stusType) throws JsonProcessingException {
        long start = System.currentTimeMillis();

        try {
            String actStusCd = auctionService.getActStusCd(reqSocketDto);

            ResSocketDto resSocketDto = null;
            if("03".equals(actStusCd)) {
                resSocketDto = resDtoService.getAuctionEnd(reqSocketDto, stusType);
            } else {
                resSocketDto = resDtoService.getAuction(reqSocketDto, stusType);
            }            
            
            String data = objectMapper.writeValueAsString(resSocketDto);
            client.sendEvent(eventName, data);
        } catch (Exception e) {
            logger.error("initSocket error : "+reqSocketDto, e);
            
            ResSocketDto resSocketDto = new ResSocketDto();
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0002");
            resSocketErrDto.setReason("요청 정보 오류");
            resSocketDto.setError(resSocketErrDto);

            String data = objectMapper.writeValueAsString(resSocketDto);
            client.sendEvent(eventName, data);
        }   

        long end = System.currentTimeMillis();
        logger.info("initSocket Execute Time : " + (end - start) +"ms");
    }

    public void bidSocket(SocketIOClient client, ReqSocketDto reqSocketDto, String eventName) throws JsonProcessingException {
        long start = System.currentTimeMillis();

        try {
            String socketId = client.getHandshakeData().getSingleUrlParam("socket_id");   
            String cacheId  = setSocketId(reqSocketDto.getService(), socketId);   
        
            HashMap<String, Object> result = redissonService.insertBid(reqSocketDto);
            ResSocketDto resSocketDto = (ResSocketDto) result.get("resSocketDto");
            UserMmbrInfm userMmbrInfm = (UserMmbrInfm) result.get("userMmbrInfm");

            if(resSocketDto.getSuccess() == 1) {
                resSocketDto = validationUtil.checkBidEtc(reqSocketDto, resSocketDto);
            }

            if(resSocketDto.getSuccess() == 1 || resSocketDto.getSuccess() == 3) {
                int sccss = resSocketDto.getSuccess();

                resSocketDto = resDtoService.getAuction(reqSocketDto, "bid");

                Map<String, Object> bidList = redisInfoService.getBidrList(reqSocketDto);
                @SuppressWarnings("unchecked")
                Set<String> bidrList = (Set<String>) bidList.get("bidrCnt");
                // Map<String, List<ResSocketBidInfoDto>> bidrList = (Map<String, List<ResSocketBidInfoDto>>) bidList.get("bidrCnt");
                
                logger.info("sendMy start");  
                auctionStusService.sendMy(reqSocketDto, bidrList);        
                logger.info("sendMy end");  

                logger.info("setMyBadgYn start");  
                auctionService.setMyBadgYn(reqSocketDto, userMmbrInfm);
                logger.info("setMyBadgYn end");  

                resSocketDto.setSuccess(sccss);
            } 

            logger.info("message sender start");  

            String data = objectMapper.writeValueAsString(resSocketDto);

            if(resSocketDto.getSuccess() == 3) {
                // 최대낙찰가로 메시지 패스
                auctionStusService.endAuction(reqSocketDto, resSocketDto, "api");
            } else if(resSocketDto.getSuccess() == 0) {
                client.sendEvent(eventName, data);
            } else if(resSocketDto.getStus_type().equals("frst_bid")) {
                client.sendEvent(eventName, data);
                Thread.sleep(500);
                resSocketDto.setStus_type("bid");
                if("02".equals(resSocketDto.getData().getAct_info().getAct_type_cd())) {
                    ResSocketDataDto resSocketDataDto = resSocketDto.getData();
                    List<ResSocketChatInfoDto> resSocketChatInfoDto = resSocketDataDto.getChat_info();
                    resSocketChatInfoDto.get(0).setChat_type("bid");
                    resSocketChatInfoDto.get(0).setChat_msg(resSocketChatInfoDto.get(0).getMmbr_nm() +"님 "+ resSocketChatInfoDto.get(0).getBid_amnt() +"원");
                    
                    resSocketDataDto.setChat_info(resSocketChatInfoDto);
                    resSocketDto.setData(resSocketDataDto);
                }
                
                data = objectMapper.writeValueAsString(resSocketDto);

                ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                redisPublisher.publish(channel, data); 
            } else {
                ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                redisPublisher.publish(channel, data); 
            }
            logger.info("message sender end");  

            if("jasonapp019".equals(reqSocketDto.getService()) && resSocketDto.getSuccess() != 0) {                
                if(reqSocketDto.getJsn_auth() == null || "".equals(reqSocketDto.getJsn_auth())) {
                } else {
                    // 게임미션 API 호출
                    if(reqSocketDto.getGame_seq() > 0){
                        auctionStusService.sendGameMssn(reqSocketDto);
                    }    

                    // 용돈봉튜 API 호출
                    auctionStusService.sendPocketMssn(reqSocketDto);
                }
            }
            
            
        } catch (Exception e) {            
            logger.error("bidSocket error : "+reqSocketDto, e);
            
            ResSocketDto resSocketDto = new ResSocketDto();
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0002");
            resSocketErrDto.setReason("요청 정보 오류");
            resSocketDto.setError(resSocketErrDto);

            String data = objectMapper.writeValueAsString(resSocketDto);
            client.sendEvent(eventName, data);
        }  

        long end = System.currentTimeMillis();
        logger.info("bidSocket Execute Time : " + (end - start) +"ms");
    }

    public void mySocket(SocketIOClient client, ReqSocketDto reqSocketDto, String eventName) throws JsonProcessingException{
        long start = System.currentTimeMillis();
        try {
            reqSocketDto.setList_cnt(0);
            ResSocketListDto resSocketListDto = resDtoService.getMyAuction(reqSocketDto);

            String data = objectMapper.writeValueAsString(resSocketListDto);

            client.sendEvent(eventName, data);
        } catch (JsonProcessingException e) {                       
            logger.error("mySocket error : "+reqSocketDto, e);
            
            ResSocketDto resSocketDto = new ResSocketDto();
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0002");
            resSocketErrDto.setReason("요청 정보 오류");
            resSocketDto.setError(resSocketErrDto);

            String data = objectMapper.writeValueAsString(resSocketDto);
            client.sendEvent(eventName, data);
        } 

        long end = System.currentTimeMillis();
        logger.info("mySocket Execute Time : " + (end - start) +"ms");
    }

    public void updateStatus(ReqSocketDto reqSocketDto, int status, String reqType) {
        reqSocketDto.setList_cnt(9);
        reqSocketDto.setLive_cnt(0);

        int    actSno = reqSocketDto.getAct_sno();
        String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

        redisCache.setRedisChannel(cacheId);  
        ResSocketDto resSocketDto = redisInfoService.checkInfo(reqSocketDto);

        switch(status) {
            case 1:
                auctionStusService.startAuction(reqSocketDto, resSocketDto);
            break;
            case 2:
                auctionStusService.pauseAuction(reqSocketDto);
            break;
            case 3:
                auctionStusService.resumeAuction(reqSocketDto);
            break;
            case 4:
                auctionStusService.endCntngAuction(reqSocketDto, resSocketDto);
            break;
            case 5:
                auctionStusService.endAuction(reqSocketDto, resSocketDto, reqType);
            break;
        }
    }

    public void updateAct(ReqSocketDto reqSocketDto) {
        int    actSno = reqSocketDto.getAct_sno();
        String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

        redisCache.setRedisChannel(cacheId);

        reqSocketDto.setList_cnt(9);
        ResSocketDto resSocketDto = redisInfoService.checkInfo(reqSocketDto);
        auctionService.updateAct(reqSocketDto, resSocketDto);
    }

    public String setSocketId(String service, String socketId) {
        String result = null;

        switch(service) {
            case "jasonapp018":
                result = "simsale:"+socketId;
                break;
            case "jasonapp014":
                result = "sale09:"+socketId;
                break;
            case "jasonapp019":
                result = "market09:"+socketId;
                break;
        }

        return result;
    }
}
