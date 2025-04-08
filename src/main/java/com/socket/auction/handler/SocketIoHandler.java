package com.socket.auction.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.socket.auction.cache.SocketCache;
import com.socket.auction.controller.AuctionController;
import com.socket.auction.cache.RedisCache;
import com.socket.auction.dto.ReqSocketDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

@Component
public class SocketIoHandler {
    
    @Autowired
    SocketCache socketCache;

    @Autowired
    RedisCache redisCache;

    @Autowired
    RedisMessageListenerContainer redisMessageListener;

    @Autowired
    AuctionController auctionController;

    // @PostConstruct
    // public void init() {
    //     RedisCache.redisChannels = new HashMap<>();
    // }

    // Triggered when the client connects, the front-end js triggers: socket = io connect(" http://localhost:9092 ")
    @OnConnect
    public void onConnect(SocketIOClient client){        
        // UUID   sessionId = client.getSessionId();
        // String socketId  = client.getHandshakeData().getSingleUrlParam("socket_id");

        // if(socketId != null) {
        //     redisCache.setRedisChannel(socketId);        
        //     socketCache.saveClient(socketId, sessionId, client);
        // }
    }

    // Trigger when the client closes the connection: front end js trigger: socket disconnect();
    @OnDisconnect
    public void onDisconnect(SocketIOClient client){
        UUID   sessionId = client.getSessionId();        
        String socketId  = client.getHandshakeData().getSingleUrlParam("socket_id");
        String service   = client.get("auction-service");

        if(service != null && socketId != null) {
            String cacheId = setSocketId(service, socketId);

            socketCache.deleteSessionClientByActSno(cacheId, sessionId);
            System.out.println("socketId: "+socketId+" Connection closed successfully - "+cacheId);

        }
    }

    /**
     * Custom message event, triggered by client js: socket emit('messageevent', {msgContent: msg}); This method is triggered when
     * Front end js socket The emit ("event name", "parameter data") method is used when triggering back-end custom message events
     * Front end js socket On ("event name", anonymous function (data sent by the server to the client)) is to listen to events on the server
     * @throws JsonProcessingException
     **/
    @OnEvent("normal")
    public void normalEvent(SocketIOClient client, ReqSocketDto reqSocketDto) throws JsonProcessingException{      
        UUID   sessionId = client.getSessionId();
        String socketId  = client.getHandshakeData().getSingleUrlParam("socket_id");
        String cacheId   = setSocketId(reqSocketDto.getService(), socketId);

        client.set("auction-service", reqSocketDto.getService());
        redisCache.setRedisChannel(cacheId);        
        socketCache.saveClient(cacheId, sessionId, client);

        // String socketId  = client.getHandshakeData().getSingleUrlParam("socket_id");
        reqSocketDto.setAct_sno(Integer.parseInt(socketId));

        switch(reqSocketDto.getMsg()) {
        case "init":
            reqSocketDto.setList_cnt(5);
            reqSocketDto.setLive_cnt(0);

            auctionController.initSocket(client, reqSocketDto, "normal", "init");
            break;
        case "more":
            reqSocketDto.setList_cnt(99);
            reqSocketDto.setLive_cnt(0);

            auctionController.initSocket(client, reqSocketDto, "normal", "more");
            break;
        case "bid":
            reqSocketDto.setList_cnt(9);
            reqSocketDto.setLive_cnt(0);

            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            reqSocketDto.setFrst_reg_dtm(regDate);

            auctionController.bidSocket(client, reqSocketDto, "normal");
            break;
        }
    }

    @OnEvent("live")
    public void liveEvent(SocketIOClient client, ReqSocketDto reqSocketDto) throws JsonProcessingException{  
        UUID   sessionId = client.getSessionId();
        String socketId  = client.getHandshakeData().getSingleUrlParam("socket_id");
        String cacheId   = setSocketId(reqSocketDto.getService(), socketId);

        client.set("auction-service", reqSocketDto.getService());
        redisCache.setRedisChannel(cacheId);        
        socketCache.saveClient(cacheId, sessionId, client);
        reqSocketDto.setAct_sno(Integer.parseInt(socketId));

        switch(reqSocketDto.getMsg()) {
        case "init":
            reqSocketDto.setList_cnt(9);
            reqSocketDto.setLive_cnt(9);

            auctionController.initSocket(client, reqSocketDto, "live", "init");
        break;
        case "more":
            reqSocketDto.setList_cnt(99);
            reqSocketDto.setLive_cnt(0);

            auctionController.initSocket(client, reqSocketDto, "live", "more");
            break;
        case "bid":
            reqSocketDto.setList_cnt(9);
            reqSocketDto.setLive_cnt(0);

            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            reqSocketDto.setFrst_reg_dtm(regDate);

            auctionController.bidSocket(client, reqSocketDto, "live");
        break;
        }
    }

    @OnEvent("my")
    public void myEvent(SocketIOClient client, ReqSocketDto reqSocketDto) throws JsonProcessingException{  
        UUID   sessionId = client.getSessionId();
        String socketId  = client.getHandshakeData().getSingleUrlParam("socket_id");
        String cacheId   = setSocketId(reqSocketDto.getService(), socketId);

        client.set("auction-service", reqSocketDto.getService());
        redisCache.setRedisChannel(cacheId);        
        socketCache.saveClient(cacheId, sessionId, client);
        reqSocketDto.setMmbr_id(socketId);

        auctionController.mySocket(client, reqSocketDto, "my");
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
            case "jasonapp019": case "localhost":
                result = "market09:"+socketId;
        }

        return result;
    }
} 