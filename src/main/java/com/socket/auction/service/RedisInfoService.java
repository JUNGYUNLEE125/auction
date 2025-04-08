package com.socket.auction.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResSocketBidInfoDto;
import com.socket.auction.dto.ResSocketChatInfoDto;
import com.socket.auction.dto.ResSocketDataDto;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.dto.ResSocketListDto;
import com.socket.auction.dto.ResSocketStusDto;
import com.socket.auction.entity.ActBidEntity;
import com.socket.auction.entity.ActEntity;
import com.socket.auction.entity.MmbrEntity;
import com.socket.auction.entity.UserEntity;
import com.socket.auction.entity.UserInfmStatEntity;
import com.socket.auction.repository.first.master.ActBidMstrRepository;
import com.socket.auction.repository.second.master.ActBidMstrScndRepository;
import com.socket.auction.repository.third.master.ActBidMstrThrdRepository;
import com.socket.auction.utils.RedisUtil;
import com.socket.auction.utils.RepositoryUtil;

@Service
public class RedisInfoService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RepositoryUtil repositoryUtil;

    @Autowired
    ActBidMstrRepository actBidMstrRepository;

    @Autowired
    ActBidMstrScndRepository actBidMstrScndRepository;

    @Autowired
    ActBidMstrThrdRepository actBidMstrThrdRepository;

    @Autowired
    AuctionService auctionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(RedisInfoService.class);

    public String getStr(String key) {
        String data = redisUtil.getStr(key);

        return data;
    }

    public List<String> getList(ReqSocketDto reqSocketDto, String key, int start, int end) {
        List<String> list = redisUtil.getList(reqSocketDto, key, start, end);

        return list;
    }

    public Long setList(ReqSocketDto reqSocketDto, String key, String value) {
        Long  result = redisUtil.setList(reqSocketDto, key, value);

        return result;
    }

    public ResSocketDto checkInfo(ReqSocketDto reqSocketDto) {
        int actSno = reqSocketDto.getAct_sno();

        ResSocketDto resSocketDto = null;
        String         infoKey = "info:"+ actSno;
        List<String>  infoList = redisUtil.getList(reqSocketDto,infoKey, 0, 0);
        
        String infoValue = null;
        if(infoList.size() == 0){
            resSocketDto = auctionService.initAuction(reqSocketDto, 1);

            try {
                infoValue = objectMapper.writeValueAsString(resSocketDto);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            redisUtil.setList(reqSocketDto, infoKey, infoValue);
            
            setEndInfo(reqSocketDto, "N");
            setBatch(reqSocketDto, "0");
        } else {
            infoValue = infoList.get(0);

            try {
                resSocketDto = objectMapper.readValue(infoValue, ResSocketDto.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return resSocketDto;
    }   

    public void setEndInfo(ReqSocketDto reqSocketDto, String useYn) {
        int actSno = reqSocketDto.getAct_sno();

        String endInfoKey = "endInfo:"+ actSno;
        redisUtil.setList(reqSocketDto, endInfoKey, useYn);
    }   

    public String getEndInfo(ReqSocketDto reqSocketDto) {
        int actSno = reqSocketDto.getAct_sno();

        String endInfoKey = "endInfo:"+ actSno;
        List<String>  endInfoList = redisUtil.getList(reqSocketDto, endInfoKey, 0, 0);
        String result = endInfoList.get(0);

        return result;
    }    

    public void setBatch(ReqSocketDto reqSocketDto, String batchList) {
        int actSno = reqSocketDto.getAct_sno();

        String batchKey = "batch:"+ actSno;
        redisUtil.setList(reqSocketDto, batchKey, batchList);
    } 

    public String setBidInfo(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        int    bidAmnt = reqSocketDto.getBid_amnt();
        String mmbrId  = reqSocketDto.getMmbr_id();
        String result  = "false";
        
        MmbrEntity mmbrEntity = repositoryUtil.mmbrByMmbrId(reqSocketDto, mmbrId);

        String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        ResSocketBidInfoDto resSocketBidInfoDto = new ResSocketBidInfoDto();
        resSocketBidInfoDto.setMmbr_id(mmbrId);
        resSocketBidInfoDto.setBid_amnt(bidAmnt);
        resSocketBidInfoDto.setMmbr_nm(mmbrEntity.getMmbrNm());
        resSocketBidInfoDto.setPrfl_img(mmbrEntity.getPrflImg());
        resSocketBidInfoDto.setReg_dtm(regDate);

        reqSocketDto.setPrfl_img(mmbrEntity.getPrflImg());
        String rankKey = "rank:"+ actSno;
        String rankValue = null;
        try {
            rankValue = objectMapper.writeValueAsString(resSocketBidInfoDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    
        redisUtil.setRank(reqSocketDto, rankKey, rankValue, bidAmnt);
        result = mmbrEntity.getMmbrNm();

        return result;
    }

    public ResSocketDataDto getRankInfo(ReqSocketDto reqSocketDto) {
        int actSno  = reqSocketDto.getAct_sno();
        int listCnt = reqSocketDto.getList_cnt();

        ResSocketDataDto          resSocketDataDto     = new ResSocketDataDto(); 
        ResSocketStusDto          resSocketStusDto     = new ResSocketStusDto();
        List<ResSocketBidInfoDto> resSocketBidInfoList = new ArrayList<ResSocketBidInfoDto>();
        
        String rankKey = "rank:"+ actSno;
        Set<TypedTuple<String>>   bidInfoList = redisUtil.getRank(reqSocketDto, rankKey, 0, listCnt);

        if(!bidInfoList.isEmpty()) {
            int i = 0;
            for(TypedTuple<String> bidInfo : bidInfoList){
                ResSocketBidInfoDto resSocketBidInfoDto = null;
                
                try {
                    resSocketBidInfoDto = objectMapper.readValue(bidInfo.getValue(), ResSocketBidInfoDto.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }    
                
                String mmbrNm  = auctionService.nameMasking(resSocketBidInfoDto.getMmbr_nm(), resSocketBidInfoDto.getMmbr_id());
                String mmbrId  = resSocketBidInfoDto.getMmbr_id();
                String prflImg = resSocketBidInfoDto.getPrfl_img();

                if(prflImg == null) {
                    prflImg = auctionService.getPrflImgUrl(reqSocketDto, "/assets/img/no_image.jpg");
                } else {
                    prflImg = auctionService.getPrflImgUrl(reqSocketDto, "/data/profile/"+ prflImg);
                }                     

                resSocketBidInfoDto.setMmbr_nm(mmbrNm);
                resSocketBidInfoDto.setMmbr_id(mmbrId);
                resSocketBidInfoDto.setBid_amnt(bidInfo.getScore());
                resSocketBidInfoDto.setPrfl_img(prflImg);
                resSocketBidInfoList.add(resSocketBidInfoDto);
                
                if(i==0) {
                    Map<String, Object> bidList = getBidrList(reqSocketDto);
                    int bidCnt = (int) bidList.get("bidCnt");
                    
                    @SuppressWarnings("unchecked")
                    Set<String> bidrCnt = (Set<String>) bidList.get("bidrCnt");
                    // Map<String, List<ResSocketBidInfoDto>> bidrCnt = (Map<String, List<ResSocketBidInfoDto>>) bidList.get("bidrCnt");

                    resSocketStusDto.setMax_bid(bidInfo.getScore());
                    resSocketStusDto.setBid_cnt(bidCnt);                        
                    resSocketStusDto.setBidr_cnt(bidrCnt.size());
                }          
                i++;
            }
        }
        
        resSocketDataDto.setAct_stus(resSocketStusDto);
        resSocketDataDto.setBid_info(resSocketBidInfoList);

        return resSocketDataDto;
    }       

    public int getBidAmnt(ReqSocketDto reqSocketDto, ResSocketDataDto resSocketDataDto) {
        // int actSno  = reqSocketDto.getAct_sno();
        // String rankKey = "rank:"+ actSno;
        // Set<TypedTuple<String>> bidInfoList = redisUtil.getRank(rankKey, 0, 0);
        
        // int result = 0;
        // if(bidInfoList.size() != 0){
        //     for(TypedTuple<String> bidInfo : bidInfoList){
        //         if(bidAmnt <= bidInfo.getScore()) {
        //             result = 1;
        //         }            
        //     }
        // }        
        
        int bidAmnt = reqSocketDto.getBid_amnt();
        int score   = (int) resSocketDataDto.getAct_stus().getMax_bid();

        int result = 0;
        if(bidAmnt <= score) {
            result = 1;
        }

        return result;
    }

    public void setBidr(ReqSocketDto reqSocketDto, String key, String value) {
        redisUtil.setBidr(reqSocketDto, key, value);
    }

    public HashMap<String, Object> getBidrList(ReqSocketDto reqSocketDto) {
        int actSno = reqSocketDto.getAct_sno();

        HashMap<String, Object> result = new HashMap<String, Object>();
        
        String bidrKey = "bidr:"+ actSno;
        String rankKey = "rank:"+ actSno;

        int rankSize = redisUtil.getRankSize(reqSocketDto, rankKey);
        Set<String> bidrList = redisUtil.getBidr(reqSocketDto, bidrKey);

        result.put("bidCnt", rankSize);
        result.put("bidrCnt", bidrList);
        return result;
    }     

    public Long setHistoryInfo(ReqSocketDto reqSocketDto) {
        int actSno = reqSocketDto.getAct_sno();
        Long  historyResult = null;

        try {
            String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            reqSocketDto.setFrst_reg_dtm(regDate);
                
            String    histryKey = "history:"+ actSno;
            String historyValue = objectMapper.writeValueAsString(reqSocketDto);
            
            historyResult = redisUtil.setList(reqSocketDto, histryKey, historyValue);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return historyResult;
    }

    public List<ResSocketChatInfoDto> getHistoryInfo(ReqSocketDto reqSocketDto, int bidBnft) {
        int actSno  = reqSocketDto.getAct_sno();
        int liveCnt = reqSocketDto.getLive_cnt();

        List<ResSocketChatInfoDto> resSocketChatInfoList = new ArrayList<ResSocketChatInfoDto>();

        String historyKey = "history:"+ actSno;
        List<String> historyInfoList = redisUtil.getList(reqSocketDto, historyKey, 0, liveCnt);
        
        if(historyInfoList.size() > 0) {               
            for(String historyInfo : historyInfoList){
                ResSocketChatInfoDto resSocketChatInfoDto = null;

                if(historyInfo.contains("chat_type")){
                    try {
                        resSocketChatInfoDto = objectMapper.readValue(historyInfo, ResSocketChatInfoDto.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }   
                } else {
                    ReqSocketDto history = null;
                    try {
                        history = objectMapper.readValue(historyInfo, ReqSocketDto.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }   

                    String mmbrNm = auctionService.nameMasking(history.getMmbr_nm(), history.getMmbr_id());
                    String mmbrId = history.getMmbr_id();  // idMasking(reqSocketDto.getMmbr_id());                    
                    String prflImg = history.getPrfl_img();

                    if(prflImg == null) {
                        prflImg = auctionService.getPrflImgUrl(reqSocketDto, "/assets/img/no_image.jpg");
                    } else {
                        prflImg = auctionService.getPrflImgUrl(reqSocketDto, "/data/profile/"+ prflImg);
                    }                     

                    resSocketChatInfoDto = new ResSocketChatInfoDto();
                    if(bidBnft == 1) {
                        resSocketChatInfoDto.setChat_type("frst_bid");
                        resSocketChatInfoDto.setChat_msg("경매에 입찰하였습니다.\n혜택 지급이 완료 되었습니다.");
                    } else {
                        resSocketChatInfoDto.setChat_type("bid");
                        resSocketChatInfoDto.setChat_msg(mmbrNm +"님 "+ history.getBid_amnt() +"원");
                    }     
                    // resSocketChatInfoDto.setChat_type("bid");
                    // resSocketChatInfoDto.setChat_msg(mmbrNm +"님 "+ history.getBid_amnt() +"원");
                    resSocketChatInfoDto.setMmbr_id(mmbrId);
                    resSocketChatInfoDto.setMmbr_nm(mmbrNm);
                    resSocketChatInfoDto.setPrfl_img(prflImg);
                    resSocketChatInfoDto.setBid_amnt(history.getBid_amnt());
                    resSocketChatInfoDto.setFrst_reg_dtm(history.getFrst_reg_dtm());
                }
                
                resSocketChatInfoList.add(resSocketChatInfoDto);
            }
        } 
        
        return resSocketChatInfoList;
    }

    public ResSocketListDto getMyAuction(ReqSocketDto reqSocketDto) {
        List<ActEntity>        actEntityList     = repositoryUtil.actByActStusCdOrderByActEdtm(reqSocketDto, "02");
        List<ResSocketDataDto> resSocketDataList = new ArrayList<ResSocketDataDto>();
        
        for(ActEntity actEntity : actEntityList) {
            int actSno = actEntity.getActSno();
            String rankKey = "rank:"+actSno;
            Set<TypedTuple<String>> bidInfoList = redisUtil.getRank(reqSocketDto, rankKey, 0, -1);            
            
            if(bidInfoList.size() != 0){
                int i=0;
                ResSocketStusDto resSocketStusDto = new ResSocketStusDto();
                for(TypedTuple<String> bidInfo : bidInfoList){
                    if(i == 0) {
                        resSocketStusDto.setMax_bid(bidInfo.getScore());
                        resSocketStusDto.setBidr_cnt(bidInfoList.size());
                    }

                    ResSocketBidInfoDto resSocketBidInfoDto = null;
                    try {
                        resSocketBidInfoDto = objectMapper.readValue(bidInfo.getValue(), ResSocketBidInfoDto.class);
                    } catch (JsonProcessingException e1) {
                        e1.printStackTrace();
                    }  

                    String mmbrId = resSocketBidInfoDto.getMmbr_id();
                    if(mmbrId.equals(reqSocketDto.getMmbr_id())) {
                        String         infoKey = "info:"+ actSno;
                        List<String>  infoList = redisUtil.getList(reqSocketDto, infoKey, 0, 0);
                        
                        String infoValue = null;
                        if(infoList.size() != 0){
                            infoValue    = infoList.get(0);
                            
                            try {
                                ResSocketDto resSocketDto = objectMapper.readValue(infoValue, ResSocketDto.class);                            
                                ResSocketDataDto resSocketDataDto = resSocketDto.getData();

                                resSocketStusDto.setMy_bid(bidInfo.getScore());
                                resSocketStusDto.setMy_bid_dtm(resSocketBidInfoDto.getReg_dtm());
                                resSocketStusDto.setAct_stus_cd(resSocketDto.getData().getAct_info().getAct_stus_cd());
                                resSocketDataDto.setAct_stus(resSocketStusDto);

                                resSocketDataList.add(resSocketDataDto);
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                    } 
                    i++;
                }
            }  
        }

        UserEntity userEntity = repositoryUtil.userByMmbrId(reqSocketDto);
        UserInfmStatEntity userInfmStatEntity = repositoryUtil.userInfmStatByUserSno(reqSocketDto, userEntity.getUserSno());
        userInfmStatEntity.setMyactBadgDsplYn("N");
        repositoryUtil.userInfmStatSave(reqSocketDto, userInfmStatEntity);

        ResSocketListDto resSocketListDto = new ResSocketListDto();
        resSocketListDto.setSuccess(1);
        resSocketListDto.setData(resSocketDataList);

        return resSocketListDto;
    }   

    public ResSocketListDto getMyBid(ReqSocketDto reqSocketDto) {
        List<ActEntity>        actEntityList     = repositoryUtil.actByActStusCdOrderByActEdtm(reqSocketDto, "02");
        List<ResSocketDataDto> resSocketDataList = new ArrayList<ResSocketDataDto>();
        
        for(ActEntity actEntity : actEntityList) {
            int actSno = actEntity.getActSno();
            String rankKey = "rank:"+actSno;
            Set<TypedTuple<String>> bidInfoList = redisUtil.getRank(reqSocketDto, rankKey, 0, -1);            
            
            if(bidInfoList.size() != 0){
                logger.info("getMyBid BidInfo Strat : "+ actSno);

                int i=0;
                ResSocketStusDto resSocketStusDto = new ResSocketStusDto();
                for(TypedTuple<String> bidInfo : bidInfoList){
                    if(i == 0) {
                        resSocketStusDto.setMax_bid(bidInfo.getScore());
                        resSocketStusDto.setBidr_cnt(bidInfoList.size());
                    }

                    ResSocketBidInfoDto resSocketBidInfoDto = null;
                    try {
                        resSocketBidInfoDto = objectMapper.readValue(bidInfo.getValue(), ResSocketBidInfoDto.class);
                    } catch (JsonProcessingException e1) {
                        e1.printStackTrace();
                    }  

                    String mmbrId = resSocketBidInfoDto.getMmbr_id();
                    if(mmbrId.equals(reqSocketDto.getMmbr_id())) {
                        String         infoKey = "info:"+ actSno;
                        List<String>  infoList = redisUtil.getList(reqSocketDto, infoKey, 0, 0);
                        
                        String infoValue = null;
                        if(infoList.size() != 0){
                            infoValue    = infoList.get(0);
                            
                            try {
                                ResSocketDto resSocketDto = objectMapper.readValue(infoValue, ResSocketDto.class);                            
                                ResSocketDataDto resSocketDataDto = resSocketDto.getData();

                                resSocketStusDto.setMy_bid(bidInfo.getScore());
                                resSocketStusDto.setMy_bid_dtm(resSocketBidInfoDto.getReg_dtm());
                                resSocketStusDto.setAct_stus_cd(resSocketDto.getData().getAct_info().getAct_stus_cd());
                                resSocketDataDto.setAct_stus(resSocketStusDto);

                                resSocketDataList.add(resSocketDataDto);
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                    } 
                    i++;
                }
                
                logger.info("getMyBid BidInfo End : "+ actSno);
            }  
        }

        ResSocketListDto resSocketListDto = new ResSocketListDto();
        resSocketListDto.setSuccess(1);
        resSocketListDto.setData(resSocketDataList);

        return resSocketListDto;
    }     

    public void insertActBid(ReqSocketDto reqSocketDto) {
        try {
            int actSno = reqSocketDto.getAct_sno();            
            String rankKey  = "rank:"+ actSno;
            String batchKey = "batch:"+ actSno;

            // int batchValue = repositoryUtil.actBidCountByActSno(reqSocketDto);
            int batchValue = Integer.parseInt(redisUtil.getList(reqSocketDto, batchKey, 0, 0).get(0));
            int rankCnt    = redisUtil.getRankSize(reqSocketDto, rankKey);
            int listCnt    = rankCnt - batchValue;                           

            if(listCnt > 0) {
                listCnt = listCnt - 1;
                ActEntity    actEntity = repositoryUtil.actByActSno(reqSocketDto);
                Set<TypedTuple<String>>  rankInfoSet  = redisUtil.getRank(reqSocketDto, rankKey, 0, listCnt);
                List<TypedTuple<String>> rankInfoList = Lists.newArrayList(rankInfoSet);

                int bidCurrMaxAmnt = 0;
                if(rankInfoList.size() > 0) {
                    Collections.reverse((List<?>) rankInfoList);

                    List<ActBidEntity> actBidList = new ArrayList<ActBidEntity>();
                    List<ActBidEntity> actBidTop  = repositoryUtil.actBidLimitByActSno(reqSocketDto, 1);
                    String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    for(TypedTuple<String> rankInfo : rankInfoList){
                        try {
                            ResSocketBidInfoDto rankInfoDto = objectMapper.readValue(rankInfo.getValue(), ResSocketBidInfoDto.class);

                            int bidAmnt = (int) rankInfoDto.getBid_amnt();
                            if(actBidTop.size() > 0 && bidAmnt == actBidTop.get(0).getBidAmnt()){
                                continue;
                            }

                            ActBidEntity actBidEntity = new ActBidEntity();
                            actBidEntity.setActSno(actEntity.getActSno());
                            actBidEntity.setPoIdx(actEntity.getPoIdx());
                            actBidEntity.setMmbrId(rankInfoDto.getMmbr_id());
                            actBidEntity.setBidAmnt(bidAmnt);
                            actBidEntity.setBidDtm(rankInfoDto.getReg_dtm());
                            actBidEntity.setFrstRedDtm(regDate);

                            actBidList.add(actBidEntity);
                            
                            bidCurrMaxAmnt = bidAmnt;                                        
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }

                    repositoryUtil.actBidSave(reqSocketDto, actBidList);

                    if(bidCurrMaxAmnt != 0) {
                        String bidrKey = "bidr:"+ actEntity.getActSno();
                        int bidCnt  = redisUtil.getRankSize(reqSocketDto, rankKey);
                        int bidrCnt = redisUtil.getBidr(reqSocketDto, bidrKey).size();

                        actEntity.setBidCurrMaxAmnt(bidCurrMaxAmnt);
                        actEntity.setBidCnt(bidCnt);
                        actEntity.setBidrCnt(bidrCnt);
                        repositoryUtil.actSave(reqSocketDto, actEntity);
                    }
                }  

                if(rankCnt > 0) {
                    String setBatchValue = Integer.toString(rankCnt);
                    redisUtil.setList(reqSocketDto, batchKey, setBatchValue);    
                }
            }
        
        } catch(Exception e) {
            logger.error("Auction End InsertActBid Asycn : "+ reqSocketDto, e);
        }
    }

    public void insertActBidHistory(ReqSocketDto reqSocketDto) {
        try {
            int    actSno  = reqSocketDto.getAct_sno();
            
            String batchKey   = "batch:"+ actSno;
            String historyKey = "history:"+ actSno;

            // String batchValue = redisUtil.popList(reqSocketDto, batchKey);
            String batchValue = redisUtil.getList(reqSocketDto, batchKey, 0, 0).get(0);
            
            int listCnt    = 0;
            int historyCnt = 0;
            int bidCurrMaxAmnt = 0;

            List<ActBidEntity> actBidList = new ArrayList<ActBidEntity>();

            if(batchValue != null) {
                historyCnt = redisUtil.getListSize(reqSocketDto, historyKey);
                listCnt    = historyCnt - Integer.parseInt(batchValue);

                if(listCnt > 0) {
                    listCnt = listCnt - 1;
                    ActEntity    actEntity = repositoryUtil.actByActSno(reqSocketDto);
                    List<String> historyInfoList = redisUtil.getList(reqSocketDto, historyKey, 0, listCnt);

                    if(historyInfoList.size() > 0) {
                        
                        String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                        Collections.reverse(historyInfoList);
                        for(String historyInfo : historyInfoList) {
                            if(historyInfo.contains("\"msg\":\"bid\"")){
                                try {
                                    ReqSocketDto history = objectMapper.readValue(historyInfo, ReqSocketDto.class);

                                    ActBidEntity actBidEntity = new ActBidEntity();
                                    actBidEntity.setActSno(actEntity.getActSno());
                                    actBidEntity.setPoIdx(actEntity.getPoIdx());
                                    actBidEntity.setMmbrId(history.getMmbr_id());
                                    actBidEntity.setBidAmnt(history.getBid_amnt());
                                    actBidEntity.setBidDtm(history.getFrst_reg_dtm());
                                    actBidEntity.setFrstRedDtm(regDate);

                                    actBidList.add(actBidEntity);

                                    bidCurrMaxAmnt = history.getBid_amnt(); 
                                } catch (JsonMappingException e) {
                                    e.printStackTrace();
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        repositoryUtil.actBidSave(reqSocketDto, actBidList);

                        if(bidCurrMaxAmnt != 0) {
                            int bidCnt  = repositoryUtil.actBidCountByActSno(reqSocketDto);
                            int bidrCnt = repositoryUtil.actBidCountByActSnoGroupByMmbrId(reqSocketDto, actEntity.getActSno()).size();

                            actEntity.setBidCurrMaxAmnt(bidCurrMaxAmnt);                                
                            actEntity.setBidCnt(bidCnt);
                            actEntity.setBidrCnt(bidrCnt);
                            repositoryUtil.actSave(reqSocketDto, actEntity); 
                        }
                    }                
                }

                batchValue = Integer.toString(historyCnt);
                redisUtil.setList(reqSocketDto, batchKey, batchValue);
            }
        
        } catch(Exception e) {
            logger.error("Auction End InsertActBid Asycn : "+ reqSocketDto, e);
        }
    }
    
}
