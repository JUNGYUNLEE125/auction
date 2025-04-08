package com.socket.auction.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.socket.auction.cache.SocketCache;
import com.socket.auction.controller.RestController;
import com.socket.auction.dto.ActFrstBidBnft;
import com.socket.auction.dto.ApiLiveChannel;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResSocketActInfoDto;
import com.socket.auction.dto.ResSocketBidInfoDto;
import com.socket.auction.dto.ResSocketDataDto;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.dto.ResSocketGodsDto;
import com.socket.auction.dto.ResSocketStusDto;
import com.socket.auction.dto.UserMmbrInfm;
import com.socket.auction.dto.log.ReqActApiErrDto;
import com.socket.auction.entity.ActBidEntity;
import com.socket.auction.entity.ActEntity;
import com.socket.auction.entity.ActSetEntity;
import com.socket.auction.entity.GdtlMngmEntity;
import com.socket.auction.entity.MmbrEntity;
import com.socket.auction.entity.PoListEntity;
import com.socket.auction.entity.UserEntity;
import com.socket.auction.entity.UserInfmStatEntity;
import com.socket.auction.utils.RedisUtil;
import com.socket.auction.utils.RepositoryUtil;
import com.socket.auction.utils.ValidationUtil;

@Service
public class AuctionService {
    
    @Value("${live.api.channel.url}")
	String liveChannelUrl;

    @Value("${live.api.end.url}")
	String liveEndUrl;
    
    @Value("${mrkt.prfl_img_url}")
	String mrktPrflImgUrl;

    @Value("${smsl.prfl_img_url}")
	String smslPrflImgUrl;

    @Value("${sale.prfl_img_url}")
	String salePrflImgUrl;
    
    @Value("${mrkt.po_img_url}")
	String mrktPoImgUrl;

    @Value("${smsl.po_img_url}")
	String smslPoImgUrl;

    @Value("${sale.po_img_url}")
	String salePoImgUrl;

    @Value("${mrkt.frst_bid_bnft}")
	String mrktFrstBidBnft;

    @Value("${smsl.frst_bid_bnft}")
	String smslFrstBidBnft;

    @Value("${sale.frst_bid_bnft}")
	String saleFrstBidBnft;

    @Value("${mrkt.act_rslt}")
	String mrktActRslt;

    @Value("${smsl.act_rslt}")
	String smslActRslt;

    @Value("${sale.act_rslt}")
	String saleActRslt;

    @Autowired
    SocketCache socketCache;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RepositoryUtil repositoryUtil; 

    @Autowired
    ValidationUtil validationUtil;

    @Autowired
    AuctionStusService auctionStusService;

    @Autowired
    ApiCommonService apiCommonService;

    @Autowired
    RestController restController;

    private RestTemplate restTemplate = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(AuctionService.class);
    
    public void sendEvent(String channel, String eventNm, String responseProductDto) {
        HashMap<UUID, SocketIOClient> userClient = socketCache.getClientByActSno(channel);
        
        if(userClient != null) {
            Iterator<Map.Entry<UUID, SocketIOClient>> iterator = userClient.entrySet().iterator();
            
            while(iterator.hasNext()){
                Map.Entry<UUID, SocketIOClient> next = iterator.next();
                next.getValue().sendEvent(eventNm, responseProductDto);
            }
        }
    } 

    public ResSocketDto initAuction(ReqSocketDto reqSocketDto, int actStusCd) {
        String actExpsYn = "Y";
        if(actStusCd == 2) actExpsYn = "N";

        ActEntity           actEntity = repositoryUtil.actByActSno(reqSocketDto);
        PoListEntity     poListEntity = repositoryUtil.poListByPoIdx(reqSocketDto, actEntity.getPoIdx());
        GdtlMngmEntity gdtlMngmEntity = repositoryUtil.gdtlMngmByPoIdx(reqSocketDto, actEntity.getPoIdx());

        String poImg    = getPoImgUrl(reqSocketDto, poListEntity.getPoImage());
        String poImgSub = getPoImgUrl(reqSocketDto, poListEntity.getPoImageSub());

        ResSocketGodsDto resSocketGodsDto = new ResSocketGodsDto();
        resSocketGodsDto.setPo_idx(poListEntity.getPoIdx());
        resSocketGodsDto.setPo_title(poListEntity.getPoTitle());
        resSocketGodsDto.setPo_oprice(poListEntity.getPoOprice());
        resSocketGodsDto.setPo_image(poImg);
        resSocketGodsDto.setPo_image_sub(poImgSub);

        ResSocketActInfoDto resSocketActInfoDto = new ResSocketActInfoDto();
        resSocketActInfoDto.setAct_sno(actEntity.getActSno());
        resSocketActInfoDto.setAct_type_cd(actEntity.getActTypeCd());
        resSocketActInfoDto.setAct_stus_cd(actEntity.getActStusCd());
        resSocketActInfoDto.setAct_rslt_cd(actEntity.getActRsltCd());
        resSocketActInfoDto.setBid_unit(actEntity.getBidUnit());
        resSocketActInfoDto.setMax_bid_unit(actEntity.getMaxBidUnit());
        resSocketActInfoDto.setAct_sdtm(actEntity.getActSdtm());
        resSocketActInfoDto.setAct_edtm(actEntity.getActEdtm());
        resSocketActInfoDto.setPo_list_option(gdtlMngmEntity.getPoListOption());
        resSocketActInfoDto.setAct_strt_price(gdtlMngmEntity.getActStrtPrice());
        resSocketActInfoDto.setAct_min_use_yn(gdtlMngmEntity.getActMinUseYn());
        resSocketActInfoDto.setAct_min_price(gdtlMngmEntity.getActMinPrice());
        resSocketActInfoDto.setAct_max_use_yn(gdtlMngmEntity.getActMaxUseYn());
        resSocketActInfoDto.setAct_max_price(gdtlMngmEntity.getActMaxPrice());
        resSocketActInfoDto.setSucs_bidr_set_cnt(actEntity.getSucsBidrSetCnt());
        resSocketActInfoDto.setWait_bidr_set_cnt(actEntity.getWaitBidrSetCnt());
        resSocketActInfoDto.setAuto_extd_use_yn(actEntity.getAutoExtdUseYn());
        resSocketActInfoDto.setAct_exps_yn(actExpsYn);
       
        ResSocketDataDto resSocketDataDto = new ResSocketDataDto();
        resSocketDataDto.setGods_info(resSocketGodsDto);
        resSocketDataDto.setAct_info(resSocketActInfoDto);

        ResSocketDto resSocketDto = new ResSocketDto();
        resSocketDto.setData(resSocketDataDto);

        return resSocketDto;
    } 

    public ResSocketDataDto getBidInfo(ReqSocketDto reqSocketDto, int minBid, int limit) {
        // List<ActBidEntity> actBidList = repositoryUtil.actBidLimitByActSnoAndBidAmnt(reqSocketDto, minBid, limit);

        // List<ActBidEntity> actBidList = repositoryUtil.actBidLimitByActSno(reqSocketDto, limit);
        List<ActBidEntity> actBidList = repositoryUtil.actBidMstrLimitByActSno(reqSocketDto, limit);

        ResSocketDataDto resSocketDataDto = new ResSocketDataDto();
        ResSocketStusDto          resSocketStusDto     = new ResSocketStusDto();
        List<ResSocketBidInfoDto> resSocketBidInfoList = new ArrayList<ResSocketBidInfoDto>();

        if(actBidList.size() > 0) {
            ActEntity actEntity = repositoryUtil.actByActSno(reqSocketDto);
            int i = 0;
            for(ActBidEntity actBidEntity : actBidList){
                ResSocketBidInfoDto resSocketBidInfoDto = new ResSocketBidInfoDto();

                MmbrEntity mmbrEntity = repositoryUtil.mmbrByMmbrId(reqSocketDto, actBidEntity.getMmbrId());
                
                String mmbrNm = nameMasking(mmbrEntity.getMmbrNm(), actBidEntity.getMmbrId());
                String prflImg = mmbrEntity.getPrflImg();

                if(prflImg == null) {
                    prflImg = getPrflImgUrl(reqSocketDto, "/assets/img/no_image.jpg");
                } else {
                    prflImg = getPrflImgUrl(reqSocketDto, "/data/profile/"+ prflImg);
                } 

                resSocketBidInfoDto.setMmbr_nm(mmbrNm);
                resSocketBidInfoDto.setMmbr_id(actBidEntity.getMmbrId());
                resSocketBidInfoDto.setBid_amnt(actBidEntity.getBidAmnt()); 
                resSocketBidInfoDto.setPrfl_img(prflImg);
                resSocketBidInfoDto.setReg_dtm(actBidEntity.getBidDtm());
                resSocketBidInfoDto.setAct_bid_sno(actBidEntity.getActBidSno());
                resSocketBidInfoDto.setBid_rslt_cd(actBidEntity.getBidRsltCd());
                resSocketBidInfoList.add(resSocketBidInfoDto);
                
                if(i==0) {
                    resSocketStusDto.setAct_stus_cd(actEntity.getActStusCd());
                    resSocketStusDto.setAct_rslt_cd(actEntity.getActRsltCd());
                    resSocketStusDto.setMax_bid(actBidEntity.getBidAmnt());
                    resSocketStusDto.setBid_cnt(actEntity.getBidCnt());
                    resSocketStusDto.setBidr_cnt(actEntity.getBidrCnt());
                }          
                i++;
            }
        }
        
        resSocketDataDto.setAct_stus(resSocketStusDto);
        resSocketDataDto.setBid_info(resSocketBidInfoList);

        return resSocketDataDto;
    }

    public ResSocketDataDto getBidInfoEnd(ReqSocketDto reqSocketDto, int limit) {
        List<ActBidEntity> actBidList = repositoryUtil.actBidLimitByActSno(reqSocketDto, limit);

        ResSocketDataDto resSocketDataDto = new ResSocketDataDto();
        ResSocketStusDto          resSocketStusDto     = new ResSocketStusDto();
        List<ResSocketBidInfoDto> resSocketBidInfoList = new ArrayList<ResSocketBidInfoDto>();

        if(actBidList.size() > 0) {
            ActEntity actEntity = repositoryUtil.actByActSno(reqSocketDto);
            int i = 0;
            for(ActBidEntity actBidEntity : actBidList){
                ResSocketBidInfoDto resSocketBidInfoDto = new ResSocketBidInfoDto();

                MmbrEntity mmbrEntity = repositoryUtil.mmbrByMmbrId(reqSocketDto, actBidEntity.getMmbrId());

                String mmbrNm  = null;
                String prflImg = null;
                if("02".equals(mmbrEntity.getMmbrStusCd()) || "03".equals(mmbrEntity.getMmbrStusCd())) {
                    mmbrNm  = "***";
                    prflImg = getPrflImgUrl(reqSocketDto, "/assets/img/no_image.jpg");
                } else {
                    mmbrNm  = nameMasking(mmbrEntity.getMmbrNm(), actBidEntity.getMmbrId());
                    prflImg = mmbrEntity.getPrflImg();

                    if(prflImg == null) {
                        prflImg = getPrflImgUrl(reqSocketDto, "/assets/img/no_image.jpg");
                    } else {
                        prflImg = getPrflImgUrl(reqSocketDto, "/data/profile/"+ prflImg);
                    } 
                }

                resSocketBidInfoDto.setMmbr_nm(mmbrNm);
                resSocketBidInfoDto.setMmbr_id(actBidEntity.getMmbrId());
                resSocketBidInfoDto.setBid_amnt(actBidEntity.getBidAmnt()); 
                resSocketBidInfoDto.setPrfl_img(prflImg);
                resSocketBidInfoDto.setReg_dtm(actBidEntity.getBidDtm());
                resSocketBidInfoDto.setAct_bid_sno(actBidEntity.getActBidSno());
                resSocketBidInfoDto.setBid_rslt_cd(actBidEntity.getBidRsltCd());
                resSocketBidInfoList.add(resSocketBidInfoDto);
                
                if(i==0) {
                    resSocketStusDto.setAct_stus_cd(actEntity.getActStusCd());
                    resSocketStusDto.setAct_rslt_cd(actEntity.getActRsltCd());
                    resSocketStusDto.setMax_bid(actBidEntity.getBidAmnt());
                    resSocketStusDto.setBid_cnt(actEntity.getBidCnt());
                    resSocketStusDto.setBidr_cnt(actEntity.getBidrCnt());
                }          
                i++;
            }
        }
        
        resSocketDataDto.setAct_stus(resSocketStusDto);
        resSocketDataDto.setBid_info(resSocketBidInfoList);

        return resSocketDataDto;
    }

    public String nameMasking(String mmbrNm, String mmbrId) {
        if("".equals(mmbrNm)) {
            return null;
        }

        String maskingName = null;
        if(mmbrNm.equals(mmbrId)) {
            maskingName = mmbrNm.substring(0, 4)+"...";
        } else if(mmbrNm.length() == 1) {
            maskingName = "*";
        } else if(mmbrNm.length() == 2) {
            maskingName = mmbrNm.substring(0, 1)+"*";
        } else if(mmbrNm.length() > 3) {
            maskingName = mmbrNm.substring(0, 2)+"**";
        } else {
            String frsName = mmbrNm.substring(0, 1);
            String midName = mmbrNm.substring(1, mmbrNm.length()-1);

            String cnvMidName = "";
            for(int i=0;i<midName.length();i++) {
                cnvMidName += "*";
            }

            String lstName = mmbrNm.substring(mmbrNm.length()-1, mmbrNm.length());
            
            maskingName = frsName + cnvMidName + lstName;
        }

        

        return maskingName;
    }

    public String idMasking(String mmbrId) {
        String frsName = mmbrId.substring(0, 2);
        String midName = mmbrId.substring(2, mmbrId.length());

        String cnvMidName = "";
        for(int i=0;i<midName.length();i++) {
            cnvMidName += "*";
        }
        
        String maskingName = frsName + cnvMidName;

        return maskingName;
    }

	public String getPrflImgUrl(ReqSocketDto reqSocketDto, String imgName) {
        String service = reqSocketDto.getService();

		String result = null;
		switch(service) {
			case "jasonapp018":
				result = smslPrflImgUrl+imgName;
			break;
			case "jasonapp014":
				result = salePrflImgUrl+imgName;
			break;
			default:
				result = mrktPrflImgUrl+imgName;
			break;
		}
		return result;
	}

	public String getPoImgUrl(ReqSocketDto reqSocketDto, String imgName) {
        String service = reqSocketDto.getService();

		String result = null;
		switch(service) {
			case "jasonapp018":
				result = smslPoImgUrl+imgName;
			break;
			case "jasonapp014":
				result = salePoImgUrl+imgName;
			break;
			default:
				result = mrktPoImgUrl+imgName;
			break;
		}
		return result;
	}

    public String getActStusCd(ReqSocketDto reqSocketDto) {
        ActEntity actEntity = repositoryUtil.actByActSno(reqSocketDto);
        String actStusCd = actEntity.getActStusCd();

        return actStusCd;
    }

    public int getFrstBidBnft(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String appType = reqSocketDto.getType();
        String mmbrId  = reqSocketDto.getMmbr_id();

        int type = 0;
        switch(appType) {
            case "aos":
                type = 1;
                break;
            case "ios":
                type = 2;
                break;
            case "web":
                type = 3;
                break;
            default:
                type = 1;
        }

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("mmbr_id", mmbrId);
        params.add("dv_type", type);
        params.add("act_sno", actSno);

        int result = 0;
        try {     
            ActFrstBidBnft actFrstBidBnft = new ActFrstBidBnft();

            switch(reqSocketDto.getService()) {
                case "jasonapp018":
                    actFrstBidBnft = restTemplate.postForObject(smslFrstBidBnft, params, ActFrstBidBnft.class);
                    break;
                case "jasonapp014":
                    actFrstBidBnft = restTemplate.postForObject(saleFrstBidBnft, params, ActFrstBidBnft.class);
                    break;
                default:
                    actFrstBidBnft = restTemplate.postForObject(mrktFrstBidBnft, params, ActFrstBidBnft.class);
            }

            int    code = actFrstBidBnft.getResult_code();
            // String msg  = actFrstBidBnft.getResult_msg();
            // if(code == 200 & "적립금 지급이 완료되었습니다.".equals(msg)) {

            if(code == 200) {            
                result = 1;
            } else {
                // error 로그 저장
                ReqActApiErrDto errDto = new ReqActApiErrDto();
                errDto.setLogType("5"); // 첫입찰 적립금
                errDto.setRqstLog(params.toString());
                errDto.setRspnsLog(actFrstBidBnft.toString());
                apiCommonService.saveApiError(reqSocketDto, errDto); 
            }
        } catch(Exception e) {
            // error 로그 저장
            ReqActApiErrDto errDto = new ReqActApiErrDto();
            errDto.setLogType("5"); // 첫입찰 적립금
            errDto.setRqstLog(params.toString());
            errDto.setRspnsLog(e.getMessage());
            apiCommonService.saveApiError(reqSocketDto, errDto); 
        }
        
        return result;
    }

    public void getPushEnd(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("act_sno", actSno);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            ReqActApiErrDto errDto = new ReqActApiErrDto();
            errDto.setLogType("6"); // 경매종료 처리 API
            errDto.setRqstLog(params.toString());
            errDto.setRspnsLog(e1.getMessage());
            apiCommonService.saveApiError(reqSocketDto, errDto); 
        }

        try {     
            ActFrstBidBnft actFrstBidBnft = new ActFrstBidBnft();
            switch(reqSocketDto.getService()) {
                case "jasonapp018":
                    actFrstBidBnft = restTemplate.postForObject(smslActRslt, params, ActFrstBidBnft.class);
                    break;
                case "jasonapp014":
                    actFrstBidBnft = restTemplate.postForObject(saleActRslt, params, ActFrstBidBnft.class);
                    break;
                default:
                    actFrstBidBnft = restTemplate.postForObject(mrktActRslt, params, ActFrstBidBnft.class);
            }           
            int code = actFrstBidBnft.getResult_code();

            if(code != 200) {            
                ReqActApiErrDto errDto = new ReqActApiErrDto();
                errDto.setLogType("6"); // 경매종료 처리 API
                errDto.setRqstLog(params.toString());
                errDto.setRspnsLog(actFrstBidBnft.toString());
                apiCommonService.saveApiError(reqSocketDto, errDto); 
            }
        } catch(Exception e) {
            // error 로그 저장
            ReqActApiErrDto errDto = new ReqActApiErrDto();
            errDto.setLogType("6"); // 경매종료 처리 API
            errDto.setRqstLog(params.toString());
            errDto.setRspnsLog(e.getMessage());
            apiCommonService.saveApiError(reqSocketDto, errDto); 
        }
    }

    public void insertActBid(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {
        ActBidEntity actBidEntity = new ActBidEntity();
        actBidEntity.setActSno(resSocketDto.getData().getAct_info().getAct_sno());
        actBidEntity.setPoIdx(resSocketDto.getData().getGods_info().getPo_idx());
        actBidEntity.setMmbrId(reqSocketDto.getMmbr_id());
        actBidEntity.setBidAmnt(reqSocketDto.getBid_amnt());
        actBidEntity.setBidDtm(reqSocketDto.getFrst_reg_dtm());
        actBidEntity.setFrstRedDtm(reqSocketDto.getFrst_reg_dtm());

        repositoryUtil.actBidSave(reqSocketDto, actBidEntity);
    }

    public void setMyBadgYn(ReqSocketDto reqSocketDto) {
        UserEntity userEntity = repositoryUtil.userByMmbrId(reqSocketDto);

        if(userEntity != null) {
            int userSno = userEntity.getUserSno();
            UserInfmStatEntity userInfmStatEntity = repositoryUtil.userInfmStatByUserSno(reqSocketDto, userSno);

            String myactBadgDsplYn = userInfmStatEntity.getMyactBadgDsplYn();
            
            if("N".equals(myactBadgDsplYn)) {
                userInfmStatEntity.setMyactBadgDsplYn("Y");
                repositoryUtil.userInfmStatSave(reqSocketDto, userInfmStatEntity);
            }
        }        
    }

    public void setMyBadgYn(ReqSocketDto reqSocketDto, UserMmbrInfm userMmbrInfm) {
        if("N".equals(userMmbrInfm.getMyactBadgDsplYn())) {
            UserInfmStatEntity userInfmStatEntity = repositoryUtil.userInfmStatByUserSno(reqSocketDto, userMmbrInfm.getUserSno());

            userInfmStatEntity.setMyactBadgDsplYn("Y");
            repositoryUtil.userInfmStatSave(reqSocketDto, userInfmStatEntity);
        }        
    }

    public void updateEndDate(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {
        repositoryUtil.actUpdateEndDtm(reqSocketDto, resSocketDto);
    }

    public void updateAct(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {
        try {
            int actSno = resSocketDto.getData().getAct_info().getAct_sno();

            String         infoKey = "info:"+ actSno;
            List<String>  infoList = redisUtil.getList(reqSocketDto, infoKey, 0, 0);
            String       infoValue = null;

            int preBidUnit    = 0;
            int preMaxBidUnit = 0;
            int actBidUnit    = 0;
            int actMaxBidUnit = 0;

            if(infoList.size() == 0){
                resSocketDto = initAuction(reqSocketDto, 1);
            } else {           
                infoValue    = infoList.get(0); 
                resSocketDto = objectMapper.readValue(infoValue, ResSocketDto.class);

                ResSocketDataDto       resSocketDataDto = resSocketDto.getData();
                ResSocketActInfoDto resSocketActInfoDto = resSocketDataDto.getAct_info();            

                ActEntity      actEntity      = repositoryUtil.actByActSno(reqSocketDto);     
                PoListEntity   poListEntity = repositoryUtil.poListByPoIdx(reqSocketDto, actEntity.getPoIdx());           
                GdtlMngmEntity gdtlMngmEntity = repositoryUtil.gdtlMngmByPoIdx(reqSocketDto, actEntity.getPoIdx());

                String poImg    = getPoImgUrl(reqSocketDto, poListEntity.getPoImage());
                String poImgSub = getPoImgUrl(reqSocketDto, poListEntity.getPoImageSub());
        
                ResSocketGodsDto resSocketGodsDto = new ResSocketGodsDto();
                resSocketGodsDto.setPo_idx(poListEntity.getPoIdx());
                resSocketGodsDto.setPo_title(poListEntity.getPoTitle());
                resSocketGodsDto.setPo_oprice(poListEntity.getPoOprice());
                resSocketGodsDto.setPo_image(poImg);
                resSocketGodsDto.setPo_image_sub(poImgSub);
                
                preBidUnit    = resSocketActInfoDto.getBid_unit();
                preMaxBidUnit = resSocketActInfoDto.getMax_bid_unit();
                actBidUnit    = actEntity.getBidUnit();
                actMaxBidUnit = actEntity.getMaxBidUnit();

                resSocketActInfoDto.setAct_sno(actEntity.getActSno());
                resSocketActInfoDto.setAct_type_cd(actEntity.getActTypeCd());
                resSocketActInfoDto.setAct_stus_cd(actEntity.getActStusCd());
                resSocketActInfoDto.setAct_rslt_cd(actEntity.getActRsltCd());
                resSocketActInfoDto.setBid_unit(actEntity.getBidUnit());
                resSocketActInfoDto.setMax_bid_unit(actEntity.getMaxBidUnit());
                resSocketActInfoDto.setAct_sdtm(actEntity.getActSdtm());
                resSocketActInfoDto.setAct_edtm(actEntity.getActEdtm());
                resSocketActInfoDto.setSucs_bidr_set_cnt(actEntity.getSucsBidrSetCnt());
                resSocketActInfoDto.setWait_bidr_set_cnt(actEntity.getWaitBidrSetCnt());
                resSocketActInfoDto.setAuto_extd_use_yn(actEntity.getAutoExtdUseYn());
                
                resSocketActInfoDto.setPo_list_option(gdtlMngmEntity.getPoListOption());
                resSocketActInfoDto.setAct_strt_price(gdtlMngmEntity.getActStrtPrice());
                resSocketActInfoDto.setAct_min_use_yn(gdtlMngmEntity.getActMinUseYn());
                resSocketActInfoDto.setAct_min_price(gdtlMngmEntity.getActMinPrice());
                resSocketActInfoDto.setAct_max_use_yn(gdtlMngmEntity.getActMaxUseYn());
                resSocketActInfoDto.setAct_max_price(gdtlMngmEntity.getActMaxPrice());

                resSocketDataDto.setGods_info(resSocketGodsDto);
                resSocketDataDto.setAct_info(resSocketActInfoDto);
                resSocketDto.setData(resSocketDataDto);
            } 

            infoValue = objectMapper.writeValueAsString(resSocketDto);
            redisUtil.setList(reqSocketDto, infoKey, infoValue);

            if(preBidUnit != actBidUnit) {
                auctionStusService.updateAuction(reqSocketDto, actBidUnit, actMaxBidUnit, "chng_unit");
            } else if(preMaxBidUnit != actMaxBidUnit) {
                auctionStusService.updateAuction(reqSocketDto, actBidUnit, actMaxBidUnit, "chng_unit");
            } else {
                auctionStusService.updateAuction(reqSocketDto, 0, 0, "chng_auct");
            }
            
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }   

    public void startAuction(ReqSocketDto reqSocketDto) {
        ActEntity      actEntity = repositoryUtil.actByActSno(reqSocketDto);
        GdtlMngmEntity gdtlMngmEntity = repositoryUtil.gdtlMngmByPoIdx(reqSocketDto, actEntity.getPoIdx());
        actEntity.setBidStrtAmnt(gdtlMngmEntity.getActStrtPrice());
        actEntity.setActStusCd("02");
        repositoryUtil.actSave(reqSocketDto, actEntity);
    }

    public void actUpdateSdtm(ReqSocketDto reqSocketDto) {
        repositoryUtil.actUpdateSdtm(reqSocketDto);
    }

    public ApiLiveChannel getLiveChannel(ReqSocketDto reqSocketDto) {
        ApiLiveChannel apiLiveChannel = new ApiLiveChannel();

        try {            
            switch(reqSocketDto.getService()) {
                case "jasonapp018":
                    apiLiveChannel = restTemplate.getForObject(liveChannelUrl+"/simsale", ApiLiveChannel.class);    
                    break;
                case "jasonapp014":
                    apiLiveChannel = restTemplate.getForObject(liveChannelUrl+"/sale09", ApiLiveChannel.class);
                    break;
                default:
                    apiLiveChannel = restTemplate.getForObject(liveChannelUrl+"/market09", ApiLiveChannel.class);
            }            
        } catch(Exception e) {
            apiLiveChannel.setAuctionFinishCount(10);
            apiLiveChannel.setAuctionRecountInterval(5);

            // error 로그 저장
            ReqActApiErrDto errDto = new ReqActApiErrDto();
            errDto.setLogType("3"); // 카운팅정보
            errDto.setRqstLog("");
            errDto.setRspnsLog(e.getMessage());
            apiCommonService.saveApiError(reqSocketDto, errDto);
        }
        
        return apiLiveChannel;
    }

    public void getLiveEnd(ReqSocketDto reqSocketDto, HashMap<String, Object> liveEnd) {
        try {
            HttpEntity<String> response = restTemplate.postForEntity(liveEndUrl, liveEnd, String.class);
            JsonNode root   = objectMapper.readTree(response.getBody());
            String result = root.path("result").toString();
            
            if(!result.contains("OK")) {
                // error 로그 저장
                ReqActApiErrDto errDto = new ReqActApiErrDto();
                errDto.setLogType("4"); // 라이브종료
                errDto.setRqstLog(liveEnd.toString());
                errDto.setRspnsLog(result.toString());
                apiCommonService.saveApiError(reqSocketDto, errDto); 
            }
        } catch (Exception e) {            
            // error 로그 저장
            ReqActApiErrDto errDto = new ReqActApiErrDto();
            errDto.setLogType("4"); // 라이브종료
            errDto.setRqstLog(liveEnd.toString());
            errDto.setRspnsLog(e.getMessage());
            apiCommonService.saveApiError(reqSocketDto, errDto); 
        }
    } 

    public HashMap<String, String> insertActBid(ReqSocketDto reqSocketDto) {
        try {
            List<ActEntity> actEntityList = repositoryUtil.actByActStusCd(reqSocketDto, "02");

            if(actEntityList.size() > 0){
                String regDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                for(ActEntity actEntity : actEntityList){
                    reqSocketDto.setAct_sno(actEntity.getActSno());
                    String rankKey  = "rank:"+ actEntity.getActSno();
                    String batchKey = "batch:"+ actEntity.getActSno();

                    // int batchValue = repositoryUtil.actBidCountByActSno(reqSocketDto);
                    int batchValue = Integer.parseInt(redisUtil.getList(reqSocketDto, batchKey, 0, 0).get(0));
                    int rankCnt    = redisUtil.getRankSize(reqSocketDto, rankKey);
                    int listCnt    = rankCnt - batchValue;                           

                    if(listCnt > 0) {
                        listCnt = listCnt - 1;
                        Set<TypedTuple<String>>  rankInfoSet  = redisUtil.getRank(reqSocketDto, rankKey, 0, listCnt);
                        List<TypedTuple<String>> rankInfoList = Lists.newArrayList(rankInfoSet);

                        int bidCurrMaxAmnt = 0;
                        if(rankInfoList.size() > 0) {
                            Collections.reverse((List<?>) rankInfoList);

                            List<ActBidEntity> actBidList = new ArrayList<ActBidEntity>();
                            List<ActBidEntity> actBidTop  = repositoryUtil.actBidLimitByActSno(reqSocketDto, 1);
                            
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
                }
            }        
        } catch(Exception e) {
            logger.error("Batch InsertActBid : "+ reqSocketDto, e);
        }        

        HashMap<String, String> result = new HashMap<String, String>();
        result.put("result", "success");

        return result;
    }    

    public MmbrEntity mmbrByMmbrId(ReqSocketDto reqSocketDto, String mmbrId) {
        MmbrEntity mmbrEntity = repositoryUtil.mmbrByMmbrId(reqSocketDto, mmbrId);

        return mmbrEntity;
    }

    public ActEntity actByActSno(ReqSocketDto reqSocketDto) {
        ActEntity actEntity = repositoryUtil.actByActSno(reqSocketDto);

        return actEntity;
    }

    public void actSave(ReqSocketDto reqSocketDto, ActEntity actEntity) {
        repositoryUtil.actSave(reqSocketDto, actEntity);
    }

    public List<ActBidEntity> actBidByActSno(ReqSocketDto reqSocketDto) {
        List<ActBidEntity> actBidList = repositoryUtil.actBidByActSno(reqSocketDto);

        return actBidList;
    }

    public List<ActBidEntity> actBidByActSnoLimit(ReqSocketDto reqSocketDto, int listCnt) {
        List<ActBidEntity> actBidList = repositoryUtil.actBidByActSnoLimit(reqSocketDto, listCnt);

        return actBidList;
    }

    public List<ActBidEntity> actBidLimitByActSnoAndBidAmnt(ReqSocketDto reqSocketDto, int actMinPrice, int listCnt) {
        List<ActBidEntity> actBidList = repositoryUtil.actBidLimitByActSnoAndBidAmnt(reqSocketDto, actMinPrice, listCnt*10);

        return actBidList;
    }

    public List<ActBidEntity> actBidActSnoAndBidAmntGreaterThanEqual(ReqSocketDto reqSocketDto, int actMinPrice) {
        List<ActBidEntity> actBidList = repositoryUtil.actBidActSnoAndBidAmntGreaterThanEqual(reqSocketDto, actMinPrice);

        return actBidList;
    }
        
    public void actBidUpdate(ReqSocketDto reqSocketDto, ActBidEntity actBidList) {
        repositoryUtil.actBidUpdate(reqSocketDto, actBidList);
    }
        
    public void actBidSave(ReqSocketDto reqSocketDto, List<ActBidEntity> actBidList) {
        repositoryUtil.actBidSave(reqSocketDto, actBidList);
    }
    
    public void actUpdateEnd(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto, String reqType) {
        repositoryUtil.actUpdateEnd(reqSocketDto, resSocketDto, reqType);
    }
    
    public UserMmbrInfm getUserMmbrInfm(ReqSocketDto reqSocketDto) {
        UserMmbrInfm userMmbrInfm = repositoryUtil.getUserMmbrInfm(reqSocketDto);

        return userMmbrInfm;
    }
    
    public ActSetEntity getActSet(ReqSocketDto reqSocketDto) {
        ActSetEntity actSetEntity = repositoryUtil.getActSet(reqSocketDto);

        return actSetEntity;
    }
}
