package com.socket.auction.cache;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Cache user - page sessionId - channel connection
/*
 * Socket 사용자 Cache에 저장
 * socketId : Socket 접속시 파라미터 socket_id(경매번호, 회원아이디)로 이용
 * Cache 저장시 socketId, SocketIOClient에 생성된 sessionId 
 * get, set 로직으로 인자값은 socketId
 */
@Component
public class SocketCache {
    
    // User Socket cache
    private static Map<String, HashMap<UUID, SocketIOClient>> concurrentHashMap = new ConcurrentHashMap<>();

    // socketId - Auction Product Number | sessionId - page sessionid | socketoclient - channel connection corresponding to page
    public void saveClient(String socketId, UUID sessionId, SocketIOClient socketIOClient){
        HashMap<UUID, SocketIOClient> sessionIdClientCache = concurrentHashMap.get(socketId);
        if(sessionIdClientCache == null){
            sessionIdClientCache = new HashMap<>();
        }
        sessionIdClientCache.put(sessionId,socketIOClient);
        concurrentHashMap.put(socketId,sessionIdClientCache);
    }

    // Get the actSno page channel information
    public HashMap<UUID,SocketIOClient> getClientByActSno(String socketId){
        return concurrentHashMap.get(socketId);
    }

    public SocketIOClient getClientBySessionId(String socketId, UUID sessionId){
        return concurrentHashMap.get(socketId).get(sessionId);
    }

    // Delete the actSno channel connection cache according to the actSno, and the cache is not used
    public void deleteUserCacheByActSno(String socketId){
        concurrentHashMap.remove(socketId);
    }

    // Delete page channel connection according to actSno and page sessionID
    public void deleteSessionClientByActSno(String socketId, UUID sessionId){
        concurrentHashMap.get(socketId).remove(sessionId);
    }

    public int logCountClient() {
        int tempUsers = 0;
        int concurrentUsers = 0;
        for(String strKey : concurrentHashMap.keySet()) {
            
            HashMap<UUID, SocketIOClient> client = concurrentHashMap.get(strKey);
            tempUsers = client.size();

            concurrentUsers = concurrentUsers + tempUsers;
        }
        
        return concurrentUsers;
    }
}