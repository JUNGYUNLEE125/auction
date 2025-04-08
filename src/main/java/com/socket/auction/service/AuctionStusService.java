package com.socket.auction.service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socket.auction.cache.RedisCache;
import com.socket.auction.controller.RestController;
import com.socket.auction.dto.ApiLiveChannel;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResSocketActInfoDto;
import com.socket.auction.dto.ResSocketBidInfoDto;
import com.socket.auction.dto.ResSocketChatInfoDto;
import com.socket.auction.dto.ResSocketDataDto;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.dto.ResSocketGodsDto;
import com.socket.auction.dto.ResSocketListDto;
import com.socket.auction.dto.ResSocketStusDto;
import com.socket.auction.utils.HttpUtils;
import com.socket.auction.utils.RedisPublisher;

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

@Service
public class AuctionStusService {

    @Autowired
    RedisCache redisCache;

    @Autowired
    ResDtoService resDtoService;

    @Autowired
    RedisPublisher redisPublisher;

    @Autowired
    RedisInfoService redisInfoService;

    @Autowired
    AuctionService auctionService;

    @Autowired
    RestController restController;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    HttpUtils httpUtils;

    @Value("${mrkt.gameApiUrl}")
	String mrktGameApiUrl;

    @Value("${smsl.gameApiUrl}")
	String smslGameApiUrl;

    @Value("${sale.gameApiUrl}")
	String saleGameApiUrl;

    @Value("${mrkt.frontapi}")
	String mrktFrontApiUrl;

    @Value("${smsl.frontapi}")
	String smslFrontApiUrl;

    @Value("${sale.frontapi}")
	String saleFrontApiUrl;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(AuctionStusService.class);

    public String setSocketId(String service, String socketId) {
        String result = null;

        switch(service) {
            case "jasonapp018":
                result = "simsale:"+ socketId;
            break;
            case "jasonapp014":
                result = "sale09:"+ socketId;
            break;
            default:
                result = "market09:"+ socketId;
        }

        return result;
    }

    public void updateAuction(ReqSocketDto reqSocketDto, int bidUnit, int maxBidUnit, String stustype) {
        try {
            int actSno = reqSocketDto.getAct_sno();
            String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

            String       infoKey   = "info:"+ actSno;
            List<String> infoList  = redisInfoService.getList(reqSocketDto, infoKey, 0, 0);
            String       infoValue = infoList.get(0);

            ResSocketDto     resSocketDto     = objectMapper.readValue(infoValue, ResSocketDto.class);
            ResSocketDataDto resSocketDataDto = resSocketDto.getData();

            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
            resSocketChatInfoDto.setChat_type(stustype);

            DecimalFormat df = new DecimalFormat("###,###");
            String dfBidUnit = df.format(bidUnit);

            if("chng_unit".equals(stustype)) {
                if(maxBidUnit == 0) {
                    resSocketChatInfoDto.setChat_msg("입찰단위가 "+ dfBidUnit +"원으로 변경되었습니다.");
                } else {
                    String dfMaxBidUnit = df.format(maxBidUnit);
                    resSocketChatInfoDto.setChat_msg("입찰단위가 "+ dfBidUnit +"원 ~ "+ dfMaxBidUnit +"원으로 변경되었습니다.");
                }
                
            } else {
                resSocketChatInfoDto.setChat_msg("NULL");
            }
            
            resSocketChatInfoDto.setMmbr_id("NULL");
            resSocketChatInfoDto.setMmbr_nm("NULL");
            resSocketChatInfoDto.setPrfl_img("NULL");
            resSocketChatInfoDto.setFrst_reg_dtm(regDate);

            String   hstrKey = "history:"+ actSno;
            String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
            Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

            if(hstyResult > 0) {
                List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                resSocketChatInfoDtoList.add(resSocketChatInfoDto);

                ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);
                List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
                ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
                resSocketStusDto.setAct_stus_cd(resSocketDataDto.getAct_info().getAct_stus_cd());

                resSocketDataDto.setAct_stus(resSocketStusDto);
                resSocketDataDto.setBid_info(resSocketBidInfoList);
                resSocketDataDto.setChat_info(resSocketChatInfoDtoList);
                    
                resSocketDto.setData(resSocketDataDto);
                resSocketDto.setSuccess(1);
                resSocketDto.setStus_type("chng_auct");

                if("02".equals(resSocketStusDto.getAct_stus_cd())) {
                    // ChannelTopic channel = redisCache.getRedisChannel(Integer.toString(actSno));
                    ChannelTopic channel = redisCache.getRedisChannel(cacheId); 
                    String data = objectMapper.writeValueAsString(resSocketDto);
                    redisPublisher.publish(channel, data); 
                }
            }   

        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendMy(ReqSocketDto reqSocketDto, Set<String> bidrList) {
        long start = System.currentTimeMillis();

        ReqSocketDto reqSocket = new ReqSocketDto();
        reqSocket.setRequest(reqSocketDto.getRequest());
        reqSocket.setType(reqSocketDto.getType());
        reqSocket.setService(reqSocketDto.getService());
        reqSocket.setAct_sno(reqSocketDto.getAct_sno());
        reqSocket.setMsg(reqSocketDto.getMsg());
        reqSocket.setMmbr_id(reqSocketDto.getMmbr_id());
        reqSocket.setMmbr_nm(reqSocketDto.getMmbr_nm());
        reqSocket.setPrfl_img(reqSocketDto.getPrfl_img());
        reqSocket.setBid_amnt(reqSocketDto.getBid_amnt());
        reqSocket.setList_cnt(reqSocketDto.getList_cnt());
        reqSocket.setLive_cnt(reqSocketDto.getLive_cnt());
        reqSocket.setFrst_reg_dtm(reqSocketDto.getFrst_reg_dtm());

        for(String bidr: bidrList) {            
            String cacheId = setSocketId(reqSocketDto.getService(), bidr);

            // ChannelTopic channel = redisCache.getRedisChannel(bidr); 
            ChannelTopic channel = redisCache.getRedisChannel(cacheId); 
            
            if(channel != null) {
                logger.info("sendMy Bidr Start : "+ bidr);

                reqSocket.setMmbr_id(bidr);
                ResSocketListDto resSocketListDto = resDtoService.getMyBid(reqSocket);

                String data = null;
                try {
                    data = objectMapper.writeValueAsString(resSocketListDto);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                redisPublisher.publish(channel, data); 
                logger.info("sendMy Bidr End : "+ bidr);
            }            
        }

        long end = System.currentTimeMillis();
        logger.info("sendMy(actSno-"+ reqSocketDto.getAct_sno() +", BidrCnt-"+ bidrList.size()+ ") Execute Time : " + (end - start) +"ms");
    }

    // public void sendMy(ReqSocketDto reqSocketDto, Map<String, List<ResSocketBidInfoDto>> bidrList) {        
    //     Set<String> keySet = bidrList.keySet();

    //     for(String bidr: keySet) {
    //         ChannelTopic channel = redisCache.getRedisChannel(bidr); 
            
    //         if(channel != null) {
    //             reqSocketDto.setMmbr_id(bidr);
    //             ResSocketListDto resSocketListDto = resDtoService.getMyAuction(reqSocketDto);

    //             String data = null;
    //             try {
    //                 data = objectMapper.writeValueAsString(resSocketListDto);
    //             } catch (JsonProcessingException e) {
    //                 e.printStackTrace();
    //             }

    //             redisPublisher.publish(channel, data); 
    //         }            
    //     }
    // }
    
    public void startAuction(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {
        try {
            int actSno = resSocketDto.getData().getAct_info().getAct_sno();
            String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

            // DB 상태 업데이트
            auctionService.startAuction(reqSocketDto);
            // ActEntity actEntity = repositoryUtil.actByActSno(reqSocketDto);
            // GdtlMngmEntity gdtlMngmEntity = repositoryUtil.gdtlMngmByPoIdx(reqSocketDto, actEntity.getPoIdx());
            // actEntity.setBidStrtAmnt(gdtlMngmEntity.getActStrtPrice());
            // actEntity.setActStusCd("02");
            // repositoryUtil.actSave(reqSocketDto, actEntity);

            ResSocketDataDto       resSocketDataDto = resSocketDto.getData();
            ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();

            resSocketActInfoDto.setAct_stus_cd("02");
            resSocketDataDto.setAct_info(resSocketActInfoDto);
            resSocketDto.setData(resSocketDataDto);

            String   infoKey = "info:"+ actSno;
            String infoValue = objectMapper.writeValueAsString(resSocketDto);            
            Long  infoResult = redisInfoService.setList(reqSocketDto, infoKey, infoValue);
            
            if(infoResult > 0){
                String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                List<ResSocketBidInfoDto> resSocketBidInfoDto = null;
                ResSocketStusDto          resSocketStusDto    = new ResSocketStusDto();
                resSocketStusDto.setAct_stus_cd("02");

                ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
                resSocketChatInfoDto.setChat_type("act_start");
                resSocketChatInfoDto.setChat_msg("경매가 시작되었습니다.");
                resSocketChatInfoDto.setMmbr_id("NULL");
                resSocketChatInfoDto.setMmbr_nm("NULL");
                resSocketChatInfoDto.setPrfl_img("NULL");
                resSocketChatInfoDto.setFrst_reg_dtm(regDate);

                List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                resSocketChatInfoDtoList.add(resSocketChatInfoDto);            

                if("02".equals(resSocketActInfoDto.getAct_type_cd())) {
                    resSocketDataDto.setChat_info(resSocketChatInfoDtoList);
                    auctionService.actUpdateSdtm(reqSocketDto);
                }
                resSocketDataDto.setAct_stus(resSocketStusDto);
                resSocketDataDto.setBid_info(resSocketBidInfoDto);

                resSocketDto.setData(resSocketDataDto);
                resSocketDto.setSuccess(1);
                resSocketDto.setStus_type("act_start");

                String   hstrKey = "history:"+ actSno;
                String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
                Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

                if(hstyResult > 0) {
                    ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                    String data = objectMapper.writeValueAsString(resSocketDto);
                    redisPublisher.publish(channel, data); 
                }            
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }          
    }

    public void pauseAuction(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

        try {
            String         infoKey = "info:"+ actSno;
            List<String>  infoList = redisInfoService.getList(reqSocketDto, infoKey, 0, 0);
            String       infoValue = infoList.get(0);

            ResSocketDto        resSocketDto        = objectMapper.readValue(infoValue, ResSocketDto.class);
            ResSocketDataDto    resSocketDataDto    = resSocketDto.getData();
            ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();

            resSocketActInfoDto.setAct_exps_yn("N");
            resSocketDataDto.setAct_info(resSocketActInfoDto);
            resSocketDto.setData(resSocketDataDto);
            
            infoValue = objectMapper.writeValueAsString(resSocketDto);        
            Long infoResult = redisInfoService.setList(reqSocketDto, infoKey, infoValue);
            
            if(infoResult > 0){
                String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
                resSocketChatInfoDto.setChat_type("act_pause");
                resSocketChatInfoDto.setChat_msg("NULL");
                resSocketChatInfoDto.setMmbr_id("NULL");
                resSocketChatInfoDto.setMmbr_nm("NULL");
                resSocketChatInfoDto.setPrfl_img("NULL");
                resSocketChatInfoDto.setFrst_reg_dtm(regDate);

                String   hstrKey = "history:"+ actSno;
                String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
                Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

                if(hstyResult > 0) {
                    List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                    resSocketChatInfoDtoList.add(resSocketChatInfoDto);

                    ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);
                    List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
                    ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
                    resSocketStusDto.setAct_stus_cd("02");

                    resSocketDataDto.setAct_stus(resSocketStusDto);
                    resSocketDataDto.setBid_info(resSocketBidInfoList);
                    resSocketDataDto.setChat_info(resSocketChatInfoDtoList);

                    resSocketDto.setData(resSocketDataDto);
                    resSocketDto.setSuccess(1);
                    
                    ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                    String data = objectMapper.writeValueAsString(resSocketDto);
                    redisPublisher.publish(channel, data); 
                }            
            } 
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }          
    }

    public void resumeAuction(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));
        try {
            String         infoKey = "info:"+ actSno;
            List<String>  infoList = redisInfoService.getList(reqSocketDto, infoKey, 0, 0);
            String       infoValue = infoList.get(0);

            ResSocketDto        resSocketDto        = objectMapper.readValue(infoValue, ResSocketDto.class);
            ResSocketDataDto    resSocketDataDto    = resSocketDto.getData();
            ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();

            resSocketActInfoDto.setAct_exps_yn("Y");
            resSocketDataDto.setAct_info(resSocketActInfoDto);
            resSocketDto.setData(resSocketDataDto);
            
            infoValue = objectMapper.writeValueAsString(resSocketDto);        
            Long infoResult = redisInfoService.setList(reqSocketDto, infoKey, infoValue);
            
            if(infoResult > 0){
                String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
                resSocketChatInfoDto.setChat_type("act_resume");
                resSocketChatInfoDto.setChat_msg("NULL");
                resSocketChatInfoDto.setMmbr_id("NULL");
                resSocketChatInfoDto.setMmbr_nm("NULL");
                resSocketChatInfoDto.setPrfl_img("NULL");
                resSocketChatInfoDto.setFrst_reg_dtm(regDate);

                String   hstrKey = "history:"+ actSno;
                String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
                Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

                if(hstyResult > 0) {
                    List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                    resSocketChatInfoDtoList.add(resSocketChatInfoDto);

                    ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);
                    List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
                    ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
                    resSocketStusDto.setAct_stus_cd("02");

                    resSocketDataDto.setAct_stus(resSocketStusDto);
                    resSocketDataDto.setBid_info(resSocketBidInfoList);
                    resSocketDataDto.setChat_info(resSocketChatInfoDtoList);
                    
                    resSocketDto.setData(resSocketDataDto);
                    resSocketDto.setSuccess(1);
                    
                    ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                    String data = objectMapper.writeValueAsString(resSocketDto);
                    redisPublisher.publish(channel, data); 
                }            
            } 
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void noticeAuction(ReqSocketDto reqSocketDto, ApiLiveChannel apiLiveChannel) {
        int    actSno = reqSocketDto.getAct_sno();
        String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

        try {
            String         infoKey = "info:"+ actSno;
            List<String>  infoList = redisInfoService.getList(reqSocketDto, infoKey, 0, 0);
            String       infoValue = infoList.get(0);

            ResSocketDto        resSocketDto        = objectMapper.readValue(infoValue, ResSocketDto.class);
            ResSocketDataDto    resSocketDataDto    = resSocketDto.getData();
            ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();            
            
            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
            resSocketChatInfoDto.setChat_type("cntdwn_dcrtn");
            resSocketChatInfoDto.setChat_msg("추가 입찰자가 없을 경우 잠시 후 카운팅 시작하겠습니다.");
            resSocketChatInfoDto.setMmbr_id("NULL");
            resSocketChatInfoDto.setMmbr_nm("NULL");
            resSocketChatInfoDto.setPrfl_img("NULL");
            resSocketChatInfoDto.setFrst_reg_dtm(regDate);

            String   hstrKey = "history:"+ actSno;
            String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
            Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

            if(hstyResult > 0) {
                resSocketActInfoDto.setAct_fnsh_cnt(apiLiveChannel.getAuctionFinishCount());
                resSocketActInfoDto.setAct_recnt_intrvl(apiLiveChannel.getAuctionRecountInterval());

                List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                resSocketChatInfoDtoList.add(resSocketChatInfoDto);

                ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);
                List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
                ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
                resSocketStusDto.setAct_stus_cd("02");

                resSocketDataDto.setAct_info(resSocketActInfoDto);
                resSocketDataDto.setAct_stus(resSocketStusDto);
                resSocketDataDto.setBid_info(resSocketBidInfoList);                    
                resSocketDataDto.setChat_info(resSocketChatInfoDtoList);
                    
                resSocketDto.setData(resSocketDataDto);
                resSocketDto.setSuccess(1);
                
                ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                String data = objectMapper.writeValueAsString(resSocketDto);
                redisPublisher.publish(channel, data); 
            } 
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }   

    public void reNoticeAuction(ReqSocketDto reqSocketDto, ApiLiveChannel apiLiveChannel) {
        int    actSno = reqSocketDto.getAct_sno();
        String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

        try {
            String         infoKey = "info:"+ actSno;
            List<String>  infoList = redisInfoService.getList(reqSocketDto, infoKey, 0, 0);
            String       infoValue = infoList.get(0);

            ResSocketDto        resSocketDto        = objectMapper.readValue(infoValue, ResSocketDto.class);
            ResSocketDataDto    resSocketDataDto    = resSocketDto.getData();
            ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();            
            
            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
            resSocketChatInfoDto.setChat_type("cntdwn_dcrtn");
            resSocketChatInfoDto.setChat_msg("");
            resSocketChatInfoDto.setMmbr_id("NULL");
            resSocketChatInfoDto.setMmbr_nm("NULL");
            resSocketChatInfoDto.setPrfl_img("NULL");
            resSocketChatInfoDto.setFrst_reg_dtm(regDate);

            String   hstrKey = "history:"+ actSno;
            String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
            Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

            if(hstyResult > 0) {
                resSocketActInfoDto.setAct_fnsh_cnt(apiLiveChannel.getAuctionFinishCount());
                resSocketActInfoDto.setAct_recnt_intrvl(apiLiveChannel.getAuctionRecountInterval());

                List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                resSocketChatInfoDtoList.add(resSocketChatInfoDto);

                ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);
                List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
                ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
                resSocketStusDto.setAct_stus_cd("02");

                resSocketDataDto.setAct_info(resSocketActInfoDto);
                resSocketDataDto.setAct_stus(resSocketStusDto);
                resSocketDataDto.setBid_info(resSocketBidInfoList);                    
                resSocketDataDto.setChat_info(resSocketChatInfoDtoList);
                    
                resSocketDto.setData(resSocketDataDto);
                resSocketDto.setSuccess(1);
                
                ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                String data = objectMapper.writeValueAsString(resSocketDto);
                redisPublisher.publish(channel, data); 
            } 
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }     

    public void cntngAuction(ReqSocketDto reqSocketDto, ApiLiveChannel apiLiveChannel) {
        int    actSno = reqSocketDto.getAct_sno();
        String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

        try {
            String         infoKey = "info:"+ actSno;
            List<String>  infoList = redisInfoService.getList(reqSocketDto, infoKey, 0, 0);
            String       infoValue = infoList.get(0);

            ResSocketDto        resSocketDto        = objectMapper.readValue(infoValue, ResSocketDto.class);
            ResSocketDataDto    resSocketDataDto    = resSocketDto.getData();
            ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();

            String actStusCd = resSocketActInfoDto.getAct_stus_cd();
            if("03".equals(actStusCd)) {
                return;
            }
            
            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
            resSocketChatInfoDto.setChat_type("cntdwn");
            resSocketChatInfoDto.setChat_msg("NULL");
            resSocketChatInfoDto.setMmbr_id("NULL");
            resSocketChatInfoDto.setMmbr_nm("NULL");
            resSocketChatInfoDto.setPrfl_img("NULL");
            resSocketChatInfoDto.setFrst_reg_dtm(regDate);

            String   hstrKey = "history:"+ actSno;
            String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
            Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

            if(hstyResult > 0) {
                resSocketActInfoDto.setAct_fnsh_cnt(apiLiveChannel.getAuctionFinishCount());
                resSocketActInfoDto.setAct_recnt_intrvl(apiLiveChannel.getAuctionRecountInterval());

                List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                resSocketChatInfoDtoList.add(resSocketChatInfoDto);

                ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);
                List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
                ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
                resSocketStusDto.setAct_stus_cd("02");

                resSocketDataDto.setAct_info(resSocketActInfoDto);
                resSocketDataDto.setAct_stus(resSocketStusDto);
                resSocketDataDto.setBid_info(resSocketBidInfoList);                    
                resSocketDataDto.setChat_info(resSocketChatInfoDtoList);
                    
                resSocketDto.setData(resSocketDataDto);
                resSocketDto.setSuccess(1);
                
                ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                String data = objectMapper.writeValueAsString(resSocketDto);
                redisPublisher.publish(channel, data); 
            }  
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }     

    @Async
    public void reCntngAuction(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {
        try {
            ApiLiveChannel apiLiveChannel = auctionService.getLiveChannel(reqSocketDto);

            reCntngAuction(reqSocketDto, apiLiveChannel);
            Thread.sleep(apiLiveChannel.getAuctionRecountInterval() * 1000);
            reNoticeAuction(reqSocketDto, apiLiveChannel);
            Thread.sleep(2 * 1000);
            cntngAuction(reqSocketDto, apiLiveChannel);
            redisInfoService.setEndInfo(reqSocketDto, "Y");
            
            for(int sec=0;sec<apiLiveChannel.getAuctionFinishCount();sec++){
                if(sec % 5 == 0){
                    String endInfo = redisInfoService.getEndInfo(reqSocketDto);
                    System.out.println(endInfo);
                    if("N".equals(endInfo)) {
                        return;
                    }
                }                
                Thread.sleep(1000);
            }

            String infoKey   = "info:"+ reqSocketDto.getAct_sno();
            String endInfo   = redisInfoService.getEndInfo(reqSocketDto);
            String infoValue = redisInfoService.getList(reqSocketDto, infoKey, 0, 0).get(0);
            String actStusCd = null;
            try {
                ResSocketDto info = objectMapper.readValue(infoValue, ResSocketDto.class);
                actStusCd = info.getData().getAct_info().getAct_stus_cd();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            
            if("Y".equals(endInfo) && "02".equals(actStusCd)) {
                endAuction(reqSocketDto, resSocketDto, "api");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }    

    public void reCntngAuction(ReqSocketDto reqSocketDto, ApiLiveChannel apiLiveChannel) {
        int    actSno = reqSocketDto.getAct_sno();
        String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));

        try {
            String         infoKey = "info:"+ actSno;
            List<String>  infoList = redisInfoService.getList(reqSocketDto, infoKey, 0, 0);
            String       infoValue = infoList.get(0);

            ResSocketDto        resSocketDto        = objectMapper.readValue(infoValue, ResSocketDto.class);
            ResSocketDataDto    resSocketDataDto    = resSocketDto.getData();
            ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();            
            
            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
            resSocketChatInfoDto.setChat_type("cntdwn_again");
            // resSocketChatInfoDto.setChat_msg("추가 입찰로"+ apiLiveChannel.getAuctionRecountInterval() +"(초) 뒤 다시 카운팅하겠습니다.");
            resSocketChatInfoDto.setChat_msg("");
            resSocketChatInfoDto.setMmbr_id("NULL");
            resSocketChatInfoDto.setMmbr_nm("NULL");
            resSocketChatInfoDto.setPrfl_img("NULL");
            resSocketChatInfoDto.setFrst_reg_dtm(regDate);

            String   hstrKey = "history:"+ actSno;
            String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
            Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

            if(hstyResult > 0) {
                resSocketActInfoDto.setAct_fnsh_cnt(apiLiveChannel.getAuctionFinishCount());
                resSocketActInfoDto.setAct_recnt_intrvl(apiLiveChannel.getAuctionRecountInterval());

                List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                resSocketChatInfoDtoList.add(resSocketChatInfoDto);

                ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);
                List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
                ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
                resSocketStusDto.setAct_stus_cd("02");

                resSocketDataDto.setAct_info(resSocketActInfoDto);
                resSocketDataDto.setAct_stus(resSocketStusDto);
                resSocketDataDto.setBid_info(resSocketBidInfoList);                    
                resSocketDataDto.setChat_info(resSocketChatInfoDtoList);
                    
                resSocketDto.setData(resSocketDataDto);
                resSocketDto.setSuccess(1);
                
                ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                String data = objectMapper.writeValueAsString(resSocketDto);
                redisPublisher.publish(channel, data); 
            } 
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void endCntngAuction(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {
        try {
            ApiLiveChannel apiLiveChannel = auctionService.getLiveChannel(reqSocketDto);

            noticeAuction(reqSocketDto, apiLiveChannel);        
            Thread.sleep(2 * 1000);
            cntngAuction(reqSocketDto, apiLiveChannel);
            redisInfoService.setEndInfo(reqSocketDto, "Y");

            for(int sec=0;sec<apiLiveChannel.getAuctionFinishCount();sec++){
                if(sec % 5 == 0){
                    String endInfo = redisInfoService.getEndInfo(reqSocketDto);
                    System.out.println(endInfo);
                    if("N".equals(endInfo)) {
                        return;
                    }
                }                
                Thread.sleep(1000);
            }
            
            String infoKey   = "info:"+ reqSocketDto.getAct_sno();
            String endInfo   = redisInfoService.getEndInfo(reqSocketDto);
            String infoValue = redisInfoService.getList(reqSocketDto, infoKey, 0, 0).get(0);
            String actStusCd = null;
            try {
                ResSocketDto info = objectMapper.readValue(infoValue, ResSocketDto.class);
                actStusCd = info.getData().getAct_info().getAct_stus_cd();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            if("Y".equals(endInfo) && "02".equals(actStusCd)) {
                // endAuction(reqSocketDto, resSocketDto, "api");
                lockEndAuction(reqSocketDto, resSocketDto, "api");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Transactional
    public String lockEndAuction(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto, String reqType){
        int    actSno = reqSocketDto.getAct_sno();
        
        String preKey = null;
        switch(reqSocketDto.getService()) {
            case "jasonapp018":
                preKey = "simsale-";
            break;
            case "jasonapp014":
                preKey = "sale09-";
            break;
            default:
                preKey = "market09-";
        }

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
            endAuction(reqSocketDto, resSocketDto, reqType);     
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

    public void endAuction(ReqSocketDto reqSocketDto, ResSocketDto resSocket, String reqType) {
        try {
            int actSno = resSocket.getData().getAct_info().getAct_sno();
            int minBid = resSocket.getData().getAct_info().getAct_min_price();
            String cacheId = setSocketId(reqSocketDto.getService(), Integer.toString(actSno));
            String infoKey = "info:"+ actSno;

            List<String> infoCheckList  = redisInfoService.getList(reqSocketDto, infoKey, 0, 0);
            String       infoCheckValue = infoCheckList.get(0);
            
            ResSocketDto info = objectMapper.readValue(infoCheckValue, ResSocketDto.class);
            String actStusCd = info.getData().getAct_info().getAct_stus_cd();

            if("03".equals(actStusCd)) {
                return;
            }

            ResSocketDto        resSocketDto        = resDtoService.updateEnd(reqSocketDto, resSocket, reqType); 
            ResSocketDataDto    resSocketDataDto    = resSocketDto.getData();
            ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();
            ResSocketGodsDto    resSocketGodsDto    = resSocket.getData().getGods_info();   
            
            resSocketDataDto.setGods_info(resSocketGodsDto);        
            resSocketDto.setData(resSocketDataDto);   

            String infoValue = objectMapper.writeValueAsString(resSocketDto);            
            redisInfoService.setList(reqSocketDto, infoKey, infoValue);
            
            String chatMsg = null;
            String actRsltCd = null;
            HashMap<String, Object> liveEnd = new HashMap<String, Object>();
            
            String channelId = null;
            switch(reqSocketDto.getService()) {
                case "jasonapp018":
                    channelId = "simsale";
                break;
                case "jasonapp014":
                    channelId = "sale09";
                break;
                default:
                    channelId = "market09";
            }

            if("01".equals(resSocketActInfoDto.getAct_rslt_cd())) {
                String bidder   = resSocketDataDto.getBid_info().get(0).getMmbr_nm();
                String mmbrId   = resSocketDataDto.getBid_info().get(0).getMmbr_id();
                Double bid_amnt = resSocketDataDto.getBid_info().get(0).getBid_amnt();

                DecimalFormat df = new DecimalFormat("###,###");
                String dfBidUnit = df.format(bid_amnt);

                chatMsg   = bidder +"님 "+ dfBidUnit +"원으로 낙찰되었습니다.";
                actRsltCd = "01";

                liveEnd.put("channel_id", channelId);
                liveEnd.put("act_sno", actSno);
                liveEnd.put("bid_amnt", bid_amnt);
                liveEnd.put("mmbr_id", mmbrId);
                liveEnd.put("bid_rslt_yn", "Y");

                // 푸시 결과 알림
                restController.getPushEnd(reqSocketDto);  
                
                // my badge update
                reqSocketDto.setMmbr_id(mmbrId);                  
                auctionService.setMyBadgYn(reqSocketDto);

            } else {
                chatMsg   = "유찰되었습니다.";
                actRsltCd = "02";

                liveEnd.put("channel_id", channelId);
                liveEnd.put("act_sno", actSno);
                liveEnd.put("bid_amnt", 0);
                liveEnd.put("mmbr_id", "");
                liveEnd.put("bid_rslt_yn", "N");
            }          
            
            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ResSocketChatInfoDto resSocketChatInfoDto = new ResSocketChatInfoDto();
            resSocketChatInfoDto.setChat_type("act_end");
            resSocketChatInfoDto.setChat_msg(chatMsg);
            resSocketChatInfoDto.setMmbr_id("NULL");
            resSocketChatInfoDto.setMmbr_nm("NULL");
            resSocketChatInfoDto.setPrfl_img("NULL");
            resSocketChatInfoDto.setFrst_reg_dtm(regDate);

            String   hstrKey = "history:"+ actSno;
            String hstyValue = objectMapper.writeValueAsString(resSocketChatInfoDto);            
            Long  hstyResult = redisInfoService.setList(reqSocketDto, hstrKey, hstyValue);

            if(hstyResult > 0) {
                List<ResSocketChatInfoDto> resSocketChatInfoDtoList = new ArrayList<ResSocketChatInfoDto>();
                resSocketChatInfoDtoList.add(resSocketChatInfoDto);

                ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);
                // List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
                List<ResSocketBidInfoDto> resSocketBidInfoList = (List<ResSocketBidInfoDto>) auctionService.getBidInfo(reqSocketDto, minBid, 10).getBid_info();
                ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
                resSocketStusDto.setAct_stus_cd("03");
                resSocketStusDto.setAct_rslt_cd(actRsltCd);
                resSocketActInfoDto.setAct_rslt_cd(actRsltCd);

                resSocketDataDto.setAct_info(resSocketActInfoDto);
                resSocketDataDto.setAct_stus(resSocketStusDto);
                resSocketDataDto.setBid_info(resSocketBidInfoList);  
                if("02".equals(resSocketActInfoDto.getAct_type_cd())) {
                    resSocketDataDto.setChat_info(resSocketChatInfoDtoList);
                }
                resSocketDto.setData(resSocketDataDto);
                resSocketDto.setSuccess(1);
                resSocketDto.setStus_type("act_end");     
                
                ChannelTopic channel = redisCache.getRedisChannel(cacheId);
                String data = objectMapper.writeValueAsString(resSocketDto);
                redisPublisher.publish(channel, data); 

                // 라이브커머스 관리자에 종료API 전송
                if("02".equals(resSocketActInfoDto.getAct_type_cd())) {
                    auctionService.getLiveEnd(reqSocketDto, liveEnd);
                    // HttpEntity<String> response = restTemplate.postForEntity(liveEndUrl, liveEnd, String.class);
                    // JsonNode root = objectMapper.readTree(response.getBody());
                    // JsonNode result = root.path("result");
                    
                    // if(!result.equals("OK")) {
                    //     // 에러 로그 
                    // }
                }                
            }   

            Map<String, Object> bidList = redisInfoService.getBidrList(reqSocketDto);
            @SuppressWarnings("unchecked")
            Set<String> bidrList = (Set<String>) bidList.get("bidrCnt");
            // Map<String, List<ResSocketBidInfoDto>> bidrList = (Map<String, List<ResSocketBidInfoDto>>) bidList.get("bidrCnt");

            sendMy(reqSocketDto, bidrList);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }    

    @Async
    public void sendGameMssn(ReqSocketDto reqSocketDto) {
        try {
            int gameSeq = reqSocketDto.getGame_seq();
            String mmbrId = reqSocketDto.getMmbr_id();
            String jsnAuth = reqSocketDto.getJsn_auth();
            String service = reqSocketDto.getService();
            
            JSONObject postData = new JSONObject();
            postData.put("mmbrId", mmbrId);
            postData.put("seq", gameSeq);
            postData.put("reqType", 1);
            postData.put("orderNumber", "0");
            postData.put("orderAmount", 0);
            postData.put("isBid", "y");

            String url = "";
            switch(service) {
                case "jasonapp014":
                    url = saleGameApiUrl;
                break;
                case "jasonapp018":
                    url = smslGameApiUrl;
                break;
                case "jasonapp019":
                    url = mrktGameApiUrl;
                break;
            }

            HashMap<String, Object> result = httpUtils.sendPostJson(url, jsnAuth, postData.toString());
            logger.info("GameMssn Request : "+ postData.toString());
            logger.info("GameMssn Response : "+ result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }  

    @Async
    public void sendPocketMssn(ReqSocketDto reqSocketDto) {
        try {
            String service = reqSocketDto.getService();
            String jsnAuth = reqSocketDto.getJsn_auth();
            
            String key = "";
            switch(service) {
                case "jasonapp014":
                    key = "remPrmtPocket_014";
                break;
                case "jasonapp018":
                    key = "remPrmtPocket_018";
                break;
                case "jasonapp019":
                    key = "remPrmtPocket_019";
                break;
            }

            String prmtPocket = redisInfoService.getStr(key);

            JSONObject pocketObj = new JSONObject(prmtPocket);

            int pocketSno = pocketObj.getInt("pocketSno");
            int actBidMoney = pocketObj.getInt("actBidMoney");            
            
            JSONObject postData = new JSONObject();
            postData.put("pocket_sno", pocketSno);
            postData.put("isue_dvsn_cd", "03");
            postData.put("isue_amnt", actBidMoney);
            postData.put("invt_hist_sno", 0);

            String url = "";
            switch(service) {
                case "jasonapp014":
                    url = saleFrontApiUrl +"/v2/pocket_isue.php";
                break;
                case "jasonapp018":
                    url = smslFrontApiUrl +"/v2/pocket_isue.php";
                break;
                case "jasonapp019":
                    url = mrktFrontApiUrl +"/v2/pocket_isue.php";
                break;
            }

            HashMap<String, Object> result = httpUtils.sendPostQuery(url, jsnAuth, postData);
            logger.info("PocketMssn Response : "+ result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
