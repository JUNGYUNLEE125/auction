package com.socket.auction.controller;

import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.socket.auction.dto.ActFrstBidBnft;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.log.ReqActApiErrDto;
import com.socket.auction.service.ApiCommonService;
import com.socket.auction.service.RedisInfoService;
import com.socket.auction.utils.RepositoryUtil;

@Controller
public class RestController {

    @Value("${mrkt.act_rslt}")
	String mrktActRslt;

    @Value("${smsl.act_rslt}")
	String smslActRslt;

    @Value("${sale.act_rslt}")
	String saleActRslt;

    @Autowired
    ApiCommonService apiCommonService;

    @Autowired
    RedisInfoService redisInfoService;

    @Autowired
    RepositoryUtil repositoryUtil;

    private RestTemplate restTemplate = new RestTemplate();
    
    @Async
    public void getPushEnd(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("act_sno", actSno);

        try {
            Thread.sleep(10000);
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

    @Async
    public Future<String> insertActBid(ReqSocketDto reqSocketDto) {
        String resultMessage = "success";

        redisInfoService.insertActBid(reqSocketDto);
        
        return new AsyncResult<>(resultMessage);
    } 
}
   