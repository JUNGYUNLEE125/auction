package com.socket.auction.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.socket.auction.controller.RestController;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResSocketActInfoDto;
import com.socket.auction.dto.ResSocketBidInfoDto;
import com.socket.auction.dto.ResSocketChatInfoDto;
import com.socket.auction.dto.ResSocketDataDto;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.dto.ResSocketErrDto;
import com.socket.auction.dto.ResSocketListDto;
import com.socket.auction.dto.ResSocketStusDto;
import com.socket.auction.entity.ActBidEntity;
import com.socket.auction.entity.ActSetEntity;
import com.socket.auction.entity.MmbrEntity;
import com.socket.auction.utils.RedisUtil;
import com.socket.auction.utils.ValidationUtil;

@Service
public class ResDtoService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ValidationUtil vaildationUtil;

    @Autowired
    AuctionService auctionService;

    @Autowired
    RedisInfoService redisInfoService;

    @Autowired
    RestController restController;

    private Logger logger = LoggerFactory.getLogger(ResDtoService.class);
    

    public ResSocketDto getAuction(ReqSocketDto reqSocketDto, String stusType) {
        int actSno = reqSocketDto.getAct_sno();

        // validation check
        HashMap<String, String> checkInfo = vaildationUtil.checkReqSocketDto(reqSocketDto);
                
        ResSocketDto resSocketDto = null;

        if("0000".equals(checkInfo.get("code"))) {
            // redis info check
            resSocketDto = redisInfoService.checkInfo(reqSocketDto);
            ResSocketDataDto resSocketDataDto = resSocketDto.getData();
            // redis rank check
            ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);

            List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
            ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
            resSocketStusDto.setAct_stus_cd(resSocketDto.getData().getAct_info().getAct_stus_cd());
            resSocketStusDto.setAct_rslt_cd(resSocketDto.getData().getAct_info().getAct_rslt_cd());

            // 처음 입찰자 체크
            int bidBnft  = 0;
            if("bid".equals(stusType)){
                int rankSize = redisUtil.getRankSize(reqSocketDto, "rank:"+actSno);
                if(rankSize == 1){    
                    bidBnft = auctionService.getFrstBidBnft(reqSocketDto);
    
                    if(bidBnft == 1) {
                        stusType = "frst_bid";
                    }
                    resSocketStusDto.setFrst_bidr_yn("Y");
                    resSocketDataDto.setAct_stus(resSocketStusDto);
                    resSocketDto.setData(resSocketDataDto);
                }
            }

            resSocketDataDto.setAct_stus(resSocketStusDto);
            resSocketDataDto.setBid_info(resSocketBidInfoList);

            if("02".equals(resSocketDataDto.getAct_info().getAct_type_cd())){
                // redis history check
                List<ResSocketChatInfoDto> resSocketChatInfoList = redisInfoService.getHistoryInfo(reqSocketDto, bidBnft);
                resSocketDataDto.setChat_info(resSocketChatInfoList);
            }

            resSocketDto.setData(resSocketDataDto);
            resSocketDto.setSuccess(1);
            resSocketDto.setStus_type(stusType);
                    
        } else {
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();
            resSocketErrDto.setCode(checkInfo.get("code"));
            resSocketErrDto.setReason(checkInfo.get("reason"));
            
            resSocketDto = new ResSocketDto();
            resSocketDto.setSuccess(0);
            resSocketDto.setError(resSocketErrDto);   
        }

        return resSocketDto;
    }
    
    public ResSocketDto getAuctionEnd(ReqSocketDto reqSocketDto, String stusType) {
        // validation check
        HashMap<String, String> checkInfo = vaildationUtil.checkReqSocketDto(reqSocketDto);
                
        ResSocketDto resSocketDto = null;

        if("0000".equals(checkInfo.get("code"))) {
            // db info check
            resSocketDto = auctionService.initAuction(reqSocketDto, 5);
            ResSocketDataDto resSocketDataDto = resSocketDto.getData();

            // db rank check
            // int minBid  = resSocketDto.getData().getAct_info().getAct_min_price();
            int listCnt = reqSocketDto.getList_cnt() + 1;
            ResSocketDataDto rankInfo = auctionService.getBidInfoEnd(reqSocketDto, listCnt);

            List<ResSocketBidInfoDto> resSocketBidInfoList = rankInfo.getBid_info();
            ResSocketStusDto          resSocketStusDto     = rankInfo.getAct_stus();
            resSocketStusDto.setAct_stus_cd(resSocketDto.getData().getAct_info().getAct_stus_cd());
            resSocketStusDto.setAct_rslt_cd(resSocketDto.getData().getAct_info().getAct_rslt_cd());

            resSocketDataDto.setAct_stus(resSocketStusDto);
            resSocketDataDto.setBid_info(resSocketBidInfoList);

            resSocketDto.setData(resSocketDataDto);
            resSocketDto.setSuccess(1);
            resSocketDto.setStus_type(stusType);
                    
        } else {
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();
            resSocketErrDto.setCode(checkInfo.get("code"));
            resSocketErrDto.setReason(checkInfo.get("reason"));
            
            resSocketDto = new ResSocketDto();
            resSocketDto.setSuccess(0);
            resSocketDto.setError(resSocketErrDto);   
        }

        return resSocketDto;
    }

    // public ResSocketDto insertBid(ReqSocketDto reqSocketDto) {
    //     ResSocketDto resSocketDto = vaildationUtil.checkBid(reqSocketDto);

    //     return resSocketDto;
    // }

    public ResSocketListDto getMyAuction(ReqSocketDto reqSocketDto) {
        ResSocketListDto resSocketListDto = redisInfoService.getMyAuction(reqSocketDto);

        return resSocketListDto;
    }

    public ResSocketListDto getMyBid(ReqSocketDto reqSocketDto) {
        ResSocketListDto resSocketListDto = redisInfoService.getMyBid(reqSocketDto);

        return resSocketListDto;
    }

    public ResSocketDto updateEnd(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto, String reqType) {
        int listCnt = resSocketDto.getData().getAct_info().getSucs_bidr_set_cnt() + resSocketDto.getData().getAct_info().getWait_bidr_set_cnt();
        ResSocketDataDto resSocketDataDto = redisInfoService.getRankInfo(reqSocketDto);

        // redisInfoService.insertActBid(reqSocketDto);
        
        Future<String> asyncFuture = restController.insertActBid(reqSocketDto);

        while(true) {
            if(asyncFuture.isDone()){
                try {
                    logger.info("End Insert act_bid Success : ", asyncFuture.get());
                } catch (InterruptedException e) {
                    logger.info("End Insert act_bid Fail : "+ e);
                } catch (ExecutionException e) {
                    logger.info("End Insert act_bid Fail : "+ e);
                }
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.info("End Insert act_bid Sleep Fail : "+ e);
            }
        }

        ResSocketActInfoDto       resSocketActInfoDto  = resSocketDto.getData().getAct_info();
        ResSocketStusDto          resSocketStusDto     = resSocketDataDto.getAct_stus();
        List<ResSocketBidInfoDto> resSocketBidInfoList = resSocketDataDto.getBid_info();

        resSocketActInfoDto.setAct_stus_cd("03");
        resSocketStusDto.setAct_stus_cd("03");

        if(resSocketBidInfoList.size() > 0) {
            if("01".equals(resSocketDto.getData().getAct_info().getAct_type_cd()) && "Y".equals(resSocketActInfoDto.getAct_min_use_yn()) && resSocketActInfoDto.getAct_min_price() > resSocketBidInfoList.get(0).getBid_amnt()){
                resSocketActInfoDto.setAct_rslt_cd("02");
                resSocketStusDto.setAct_rslt_cd("02");
            } else {
                resSocketActInfoDto.setAct_rslt_cd("01");
                resSocketStusDto.setAct_rslt_cd("01");

                // List<ActBidEntity> actBidList = auctionService.actBidLimitByActSnoAndBidAmnt(reqSocketDto, resSocketActInfoDto.getAct_min_price(), listCnt*10);
                // List<ActBidEntity> actBidList = auctionService.actBidByActSno(reqSocketDto);
                List<ActBidEntity> actBidList = null;
                if("01".equals(resSocketDto.getData().getAct_info().getAct_type_cd())) {
                    actBidList = auctionService.actBidActSnoAndBidAmntGreaterThanEqual(reqSocketDto, resSocketActInfoDto.getAct_min_price());
                } else {
                    actBidList = auctionService.actBidActSnoAndBidAmntGreaterThanEqual(reqSocketDto, 0);
                }
                
                int i=0;     
                String             nowDate          = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                List<ActBidEntity> actBidEntityList = new ArrayList<ActBidEntity>();
                List<String>       mmbrTlnoList     = new ArrayList<String>();
                List<String>       mmbrIdList       = new ArrayList<String>();
                MmbrEntity         mmbrEntity       = null;
                for(ActBidEntity actBidEntity : actBidList) {
                    String mmbrId = actBidEntity.getMmbrId();
                    mmbrEntity = auctionService.mmbrByMmbrId(reqSocketDto, actBidEntity.getMmbrId());

                    if(i==0){
                        String payPsblDtm = null;
                        ActSetEntity actSetEntity = auctionService.getActSet(reqSocketDto);
                        
                        switch(actSetEntity.getPayExprCd()){
                            case "01":
                                payPsblDtm = LocalDateTime.now().plusHours(24).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"))+":59:59";
                                break;
                            case "02":
                                payPsblDtm = LocalDate.now().plusDays(1)+" 23:59:59";
                                break;
                            case "03":
                                payPsblDtm = LocalDate.now().plusDays(2)+" 23:59:59";
                                break;
                            case "04":
                                payPsblDtm = LocalDate.now().plusDays(3)+" 23:59:59";
                                break;
                            case "05":
                                payPsblDtm = LocalDate.now().plusDays(4)+" 23:59:59";
                                break;
                            default :
                                payPsblDtm = LocalDate.now().plusDays(1)+" 23:59:59";                            
                        }

                        actBidEntity.setBidRsltCd("01");                        
                        actBidEntity.setWaitBidrSeq(Integer.toString(i));
                        actBidEntity.setSucsBidDtm(nowDate);
                        actBidEntity.setPayPsblDtm(payPsblDtm);
                        actBidEntity.setFinlEditDtm(nowDate);
                        actBidEntity.setFinlEdtrNs("sockSystm");
                    } else {                        
                        if(mmbrTlnoList.contains(mmbrEntity.getMmbrTlno())) {
                            // actBidEntity.setBidRsltCd("04");
                            // actBidEntity.setFinlEditDtm(nowDate);
                            // actBidEntity.setFinlEdtrNs("sockSystm");
                            continue;
                        }

                        if(mmbrIdList.contains(mmbrId)) {
                            continue;
                        } else {                            
                            actBidEntity.setBidRsltCd("03");
                            actBidEntity.setWaitBidrSeq(Integer.toString(i)); 
                            actBidEntity.setFinlEditDtm(nowDate);
                            actBidEntity.setFinlEdtrNs("sockSystm");
                        }                        
                    }                            
                    
                    if(mmbrEntity.getMmbrTlno() == null || "".equals(mmbrEntity.getMmbrTlno())){
                    } else {
                        mmbrTlnoList.add(mmbrEntity.getMmbrTlno());
                    }
                    
                    if(mmbrId == null || "".equals(mmbrId)){
                    } else {
                        mmbrIdList.add(mmbrId);
                    }

                    // auctionService.actBidUpdate(reqSocketDto, actBidEntity);

                    actBidEntityList.add(actBidEntity);
                    i++;

                    if(i == listCnt) {
                        break;
                    }
                }

                auctionService.actBidSave(reqSocketDto, actBidEntityList);
            }
        } else {
            resSocketActInfoDto.setAct_rslt_cd("02");
            resSocketStusDto.setAct_rslt_cd("02");
        }
        
        resSocketDataDto.setAct_stus(resSocketStusDto);
        resSocketDataDto.setBid_info(resSocketBidInfoList);
        resSocketDataDto.setAct_info(resSocketActInfoDto);
        resSocketDataDto.setGods_info(resSocketDto.getData().getGods_info());
        resSocketDto.setData(resSocketDataDto);

        auctionService.actUpdateEnd(reqSocketDto, resSocketDto, reqType);
        
        return resSocketDto;
    }
}
