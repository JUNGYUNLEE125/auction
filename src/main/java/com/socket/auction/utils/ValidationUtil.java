package com.socket.auction.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResSocketActInfoDto;
import com.socket.auction.dto.ResSocketDataDto;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.dto.ResSocketErrDto;
import com.socket.auction.dto.UserMmbrInfm;
import com.socket.auction.service.AuctionService;
import com.socket.auction.service.AuctionStusService;
import com.socket.auction.service.RedisInfoService;

@Component
public class ValidationUtil {

    @Autowired
    RedisInfoService redisInfoService;

    @Autowired
    AuctionService auctionService;

    @Autowired
    AuctionStusService auctionStusService;

    private Logger logger = LoggerFactory.getLogger(ValidationUtil.class);
    
    // Socket 접속 요청시 유효성 검사
    public HashMap<String, String> checkReqSocketDto(ReqSocketDto reqSocketDto) {
        String request = reqSocketDto.getRequest();
        String dvType  = reqSocketDto.getType();
        String msg     = reqSocketDto.getMsg();
        String mmbrId  = reqSocketDto.getMmbr_id();
        int    bidAmnt = reqSocketDto.getBid_amnt();

        HashMap<String, String> result = new HashMap<String, String>();
        result.put("code", "0000");
        result.put("reason", "");

        if(!"auction".equals(request)) {
            result.put("code", "0002");
            result.put("reason", "Request 요청 오류");
            return result;
        }

        if(dvType.isEmpty()) {
            result.put("code", "0003");
            result.put("reason", "type 요청 오류");            
            return result;
        } 
        
        if("aos".equals(dvType) || "ios".equals(dvType) || "web".equals(dvType)) {
        } else {
            result.put("code", "0003");
            result.put("reason", "type 요청 오류");
            return result;
        }

        if(msg.isEmpty()) {
            result.put("code", "0004");
            result.put("reason", "msg 요청 오류");            
            return result;
        } 
        
        if("init".equals(msg) || "bid".equals(msg) || "more".equals(msg)) {            
        } else {
            result.put("code", "0004");
            result.put("reason", "msg 요청 오류");
            return result;
        }           

        if("bid".equals(msg)) {
            if(bidAmnt == 0){
                result.put("code", "0005");
                result.put("reason", "bid_amnt 요청 오류");
                return result;
            }
        
            if(mmbrId.isEmpty()) {
                result.put("code", "0006");
                result.put("reason", "mmbr_id 요청 오류");            
                return result;
            }
        }

        return result;
    }

    // Socket 입찰시 유효성 검사
    public HashMap<String, Object> checkBid(ReqSocketDto reqSocketDto) {    
        ResSocketDto resSocketDto = redisInfoService.checkInfo(reqSocketDto);
        resSocketDto.setSuccess(1);

        ResSocketDataDto rankInfo = redisInfoService.getRankInfo(reqSocketDto);    

        logger.info("회원 유무 체크, 패널티 회원체크 시작");
        // 회원 유무 체크
        // MmbrEntity mmbrEntity = auctionService.mmbrByMmbrId(reqSocketDto, reqSocketDto.getMmbr_id());
        UserMmbrInfm userMmbrInfm = auctionService.getUserMmbrInfm(reqSocketDto);
        // if(mmbrEntity == null) {
        if(userMmbrInfm.getMmbrId() == null) {
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0006");
            resSocketErrDto.setReason("mmbr_id 요청 오류");
            resSocketDto.setError(resSocketErrDto);           

            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("resSocketDto", resSocketDto);
            result.put("userMmbrInfm", null);
                    
            return result;
            // return resSocketDto;
        }

        // 패널티 회원 체크
        // if("Y".equals(mmbrEntity.getActPntYn())) {
        if("Y".equals(userMmbrInfm.getActPntYn())) {
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0019");
            switch(reqSocketDto.getService()) {
                case "jasonapp019":
                    resSocketErrDto.setReason("패널티기간에는 경매에 참여할 수 없습니다.\nMY공구 > 경매 참여내역에서 확인하세요.");    
                    break;
                case "jasonapp018":
                    resSocketErrDto.setReason("패널티기간에는 경매에 참여할 수 없습니다.\nMY심쿵 > 경매 참여내역에서 확인하세요.");    
                    break;
                case "jasonapp014" :
                    resSocketErrDto.setReason("패널티기간에는 경매에 참여할 수 없습니다.\nMY할인 > 경매 참여내역에서 확인하세요.");    
                    break;
                default:
                    resSocketErrDto.setReason("패널티기간에는 경매에 참여할 수 없습니다.\nMY > 경매 참여내역에서 확인하세요."); 
            }

            resSocketDto.setError(resSocketErrDto);           

            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("resSocketDto", resSocketDto);
            result.put("userMmbrInfm", null);
                    
            return result;
            // return resSocketDto;
        }
        logger.info("회원 유무 체크, 패널티 회원체크 끝");
        
        logger.info("경매 진행중 일시정지, 이전 입찰자 체크 시작");
        // 경매 진행중 상태 체크
        // String actStusCd = auctionService.getActStusCd(reqSocketDto);
        String actStusCd = resSocketDto.getData().getAct_info().getAct_stus_cd();
        if(!"02".equals(actStusCd)) {
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0012");
            resSocketErrDto.setReason("현재 경매 진행 중이 아닙니다.");
            resSocketDto.setError(resSocketErrDto);           

            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("resSocketDto", resSocketDto);
            result.put("userMmbrInfm", null);
                    
            return result;
            // return resSocketDto;
        }    
        
        // 경매 일시정지 상태 체크
        String actExpsYn = resSocketDto.getData().getAct_info().getAct_exps_yn();
        if("N".equals(actExpsYn)) {
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0017");
            resSocketErrDto.setReason("경매가 잠시 중단되었습니다.\n잠시 후 다시 입찰해 주세요.");
            resSocketDto.setError(resSocketErrDto);           

            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("resSocketDto", resSocketDto);
            result.put("userMmbrInfm", null);
                    
            return result;
            // return resSocketDto;
        }

        // 이전 입찰자 체크
        if(rankInfo.getBid_info().size() > 0) {
            String preBidr = rankInfo.getBid_info().get(0).getMmbr_id();
            String nxtBidr = reqSocketDto.getMmbr_id();

            if(preBidr.equals(nxtBidr)) {
                resSocketDto.setSuccess(0);
                ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

                resSocketErrDto.setCode("0016");
                resSocketErrDto.setReason("현재 최고가로 입찰 중입니다.");
                resSocketDto.setError(resSocketErrDto);           

                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("resSocketDto", resSocketDto);
                result.put("userMmbrInfm", null);
                        
                return result;
                // return resSocketDto;
            }
        }
        logger.info("경매 진행중 일시정지, 이전 입찰자 체크 끝");

        if("Y".equals(resSocketDto.getData().getAct_info().getAct_max_use_yn()) && resSocketDto.getData().getAct_info().getAct_max_price() <= reqSocketDto.getBid_amnt()) {

            logger.info("경매 최고낙찰가1 체크 시작");
            reqSocketDto.setBid_amnt(resSocketDto.getData().getAct_info().getAct_max_price());
            logger.info("경매 최고낙찰가1 체크 시작");

            logger.info("현재 입찰가 최고가 체크 시작");
            // 현재 입찰가 최고가 인지 체크
            int checkBid = redisInfoService.getBidAmnt(reqSocketDto, rankInfo);
            if(checkBid == 1) {
                resSocketDto.setSuccess(0);
                ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

                resSocketErrDto.setCode("0011");
                resSocketErrDto.setReason("현재 최고가보다 높은 금액을 입찰해주세요.");
                resSocketDto.setError(resSocketErrDto);           

                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("resSocketDto", resSocketDto);
                result.put("userMmbrInfm", null);
                        
                return result;
                // return resSocketDto;
            } 
            logger.info("현재 입찰가 최고가 체크 끝");

        } else {

            logger.info("시작입찰가, 입찰단위, 최대입찰단위 체크 시작");
            // 시작입찰가, 입찰단위, 최대입찰단위 체크        
            int preAmnt = (int) rankInfo.getAct_stus().getMax_bid();
            int strAmnt = resSocketDto.getData().getAct_info().getAct_strt_price();
            int bidUnit = resSocketDto.getData().getAct_info().getBid_unit();
            int maxUnit = resSocketDto.getData().getAct_info().getMax_bid_unit();
            int nxtAmnt = reqSocketDto.getBid_amnt();
            int bidAmnt = nxtAmnt - preAmnt;
            int chkUnit = bidAmnt % bidUnit;

            if(rankInfo.getBid_info().size() > 0) {
                // 최대입찰단위 체크
                if(maxUnit != 0 && bidAmnt > maxUnit) {
                    resSocketDto.setSuccess(0);
                    ResSocketErrDto resSocketErrDto = new ResSocketErrDto();
        
                    resSocketErrDto.setCode("0014");
                    resSocketErrDto.setReason("입찰 범위 내에서 입찰이 가능합니다.");
                    resSocketDto.setError(resSocketErrDto);           

                    HashMap<String, Object> result = new HashMap<String, Object>();
                    result.put("resSocketDto", resSocketDto);
                    result.put("userMmbrInfm", null);
                            
                    return result;    
                    // return resSocketDto;
                }      

                // 입찰단위 체크
                if(chkUnit != 0) {
                    resSocketDto.setSuccess(0);
                    ResSocketErrDto resSocketErrDto = new ResSocketErrDto();
        
                    resSocketErrDto.setCode("0015");
                    resSocketErrDto.setReason("입찰 범위 내에서 입찰이 가능합니다.");
                    resSocketDto.setError(resSocketErrDto);           

                    HashMap<String, Object> result = new HashMap<String, Object>();
                    result.put("resSocketDto", resSocketDto);
                    result.put("userMmbrInfm", null);
                            
                    return result;    
                    // return resSocketDto;
                }
            } else {
                // 시작입찰가 체크
                if(bidAmnt != strAmnt) {
                    resSocketDto.setSuccess(0);
                    ResSocketErrDto resSocketErrDto = new ResSocketErrDto();
        
                    resSocketErrDto.setCode("0013");
                    resSocketErrDto.setReason("입찰 범위 내에서 입찰이 가능합니다.");
                    resSocketDto.setError(resSocketErrDto);           

                    HashMap<String, Object> result = new HashMap<String, Object>();
                    result.put("resSocketDto", resSocketDto);
                    result.put("userMmbrInfm", null);
                            
                    return result;    
                    // return resSocketDto;
                }
            }
            logger.info("시작입찰가, 입찰단위, 최대입찰단위 체크 끝");
        }

        logger.info("현재 입찰가 최고가 체크 시작");
        // 현재 입찰가 최고가 인지 체크
        int checkBid = redisInfoService.getBidAmnt(reqSocketDto, rankInfo);
        if(checkBid == 1) {
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0011");
            resSocketErrDto.setReason("현재 최고가보다 높은 금액을 입찰해주세요.");
            resSocketDto.setError(resSocketErrDto);           

            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("resSocketDto", resSocketDto);
            result.put("userMmbrInfm", null);
                    
            return result;
            // return resSocketDto;
        } 
        logger.info("현재 입찰가 최고가 체크 끝");

        // rank 데이터 입력
        String checkBidInfo = redisInfoService.setBidInfo(reqSocketDto);
        if("false".equals(checkBidInfo)) {
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0021");
            resSocketErrDto.setReason("소켓서버 : Redis 랭킹 입력 장애");
            resSocketDto.setError(resSocketErrDto);           

            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("resSocketDto", resSocketDto);
            result.put("userMmbrInfm", null);
                    
            return result;
            // return resSocketDto;
        }

        // history 데이터 입력
        reqSocketDto.setMmbr_nm(checkBidInfo);
        Long checkHistoryInfo = redisInfoService.setHistoryInfo(reqSocketDto);
        if(checkHistoryInfo == 0) {
            resSocketDto.setSuccess(0);
            ResSocketErrDto resSocketErrDto = new ResSocketErrDto();

            resSocketErrDto.setCode("0022");
            resSocketErrDto.setReason("소켓서버 : Redis 이력 입력 장애");
            resSocketDto.setError(resSocketErrDto);           

            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("resSocketDto", resSocketDto);
            result.put("userMmbrInfm", null);
                    
            return result;
            // return resSocketDto;
        }

        // bidr 데이터 입력
        String bidrKey = "bidr:"+reqSocketDto.getAct_sno();
        String bidrValue = reqSocketDto.getMmbr_id();
        redisInfoService.setBidr(reqSocketDto, bidrKey, bidrValue);

        // // 일반경매 자동마감연장 체크
        // String endInfo   = redisInfoService.getEndInfo(reqSocketDto);
        // String actTypeCd = resSocketDto.getData().getAct_info().getAct_type_cd();
        // String extdUseYn = resSocketDto.getData().getAct_info().getAuto_extd_use_yn();
        
        // if("01".equals(actTypeCd) && "Y".equals(extdUseYn) && "N".equals(endInfo)) {
        //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //     LocalDateTime currentDate = LocalDateTime.now();
        //     String        actEdtm     = resSocketDto.getData().getAct_info().getAct_edtm();            
        //     LocalDateTime endDate     = LocalDateTime.parse(actEdtm, formatter);

        //     Duration duration = Duration.between(currentDate, endDate);
        //     if(duration.getSeconds() > 0 && duration.getSeconds() < 300) {
        //         LocalDateTime plusDate = endDate.plusMinutes(5);
                
        //         ResSocketDataDto resSocketDataDto = resSocketDto.getData();
        //         ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();
        //         resSocketActInfoDto.setAct_edtm(plusDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
        //         resSocketDataDto.setAct_info(resSocketActInfoDto);
        //         resSocketDto.setData(resSocketDataDto);
                
        //         redisInfoService.setEndInfo(reqSocketDto, "Y");
        //         auctionService.updateEndDate(reqSocketDto, resSocketDto);
        //         auctionService.updateAct(reqSocketDto, resSocketDto);
        //     }            
        // }

        // // 라이브경매 마감카운팅
        // if("02".equals(actTypeCd) && "Y".equals(endInfo)) {
        //     redisInfoService.setEndInfo(reqSocketDto, "N");
        //     auctionStusService.reCntngAuction(reqSocketDto, resSocketDto);           
        // }

        // // 최고낙찰가 체크
        // Boolean checkMaxBidYn = getMaxBidYn(reqSocketDto, resSocketDto);
        // if(checkMaxBidYn) {
        //     resSocketDto.setSuccess(3);
        // } else {
        //     resSocketDto.setSuccess(1);
        // }

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("resSocketDto", resSocketDto);
        result.put("userMmbrInfm", userMmbrInfm);
        
        return result;
    }

    public ResSocketDto checkBidEtc(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {  
        logger.info("일반경매 자동마감연장 체크 시작");  
        // 일반경매 자동마감연장 체크
        String endInfo   = redisInfoService.getEndInfo(reqSocketDto);
        String actTypeCd = resSocketDto.getData().getAct_info().getAct_type_cd();
        String extdUseYn = resSocketDto.getData().getAct_info().getAuto_extd_use_yn();
        
        if("01".equals(actTypeCd) && "Y".equals(extdUseYn) && "N".equals(endInfo)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LocalDateTime currentDate = LocalDateTime.now();
            String        actEdtm     = resSocketDto.getData().getAct_info().getAct_edtm();            
            LocalDateTime endDate     = LocalDateTime.parse(actEdtm, formatter);

            Duration duration = Duration.between(currentDate, endDate);
            if(duration.getSeconds() > 0 && duration.getSeconds() < 300) {
                LocalDateTime plusDate = endDate.plusMinutes(5);
                
                ResSocketDataDto resSocketDataDto = resSocketDto.getData();
                ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();
                resSocketActInfoDto.setAct_edtm(plusDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                resSocketDataDto.setAct_info(resSocketActInfoDto);
                resSocketDto.setData(resSocketDataDto);
                
                redisInfoService.setEndInfo(reqSocketDto, "Y");
                auctionService.updateEndDate(reqSocketDto, resSocketDto);
                auctionService.updateAct(reqSocketDto, resSocketDto);
            }            
        }
        logger.info("일반경매 자동마감연장 체크 끝");  

        // 라이브경매 마감카운팅
        // logger.info("라이브경매 마감카운팅 체크 시작");          
        // if("02".equals(actTypeCd) && "Y".equals(endInfo)) {
        //     redisInfoService.setEndInfo(reqSocketDto, "N");
        //     auctionStusService.reCntngAuction(reqSocketDto, resSocketDto);           
        // }
        // logger.info("라이브경매 마감카운팅 체크 끝");  
        
        logger.info("최고낙찰가 체크2 시작");  
        // 최고낙찰가 체크
        Boolean checkMaxBidYn = getMaxBidYn(reqSocketDto, resSocketDto);
        if(checkMaxBidYn) {
            resSocketDto.setSuccess(3);
        }
        logger.info("최고낙찰가 체크2 끝");  
        
        return resSocketDto;
    }

    public Boolean getMaxBidYn(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {
        Boolean result = false;
        // 일반경매일때 최고 낙찰가 적용 > 라이브 경매도 적용(22.08.03)
        // if("01".equals(resSocketDto.getData().getAct_info().getAct_type_cd())) {
            if("Y".equals(resSocketDto.getData().getAct_info().getAct_max_use_yn())) {
                if(resSocketDto.getData().getAct_info().getAct_max_price() <= reqSocketDto.getBid_amnt()){
                    // auctionStusService.endAuction(reqSocketDto, resSocketDto);                
                    result = true;
                }
            }
        // }
        
        return result;
    }    
}
