package com.socket.auction.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socket.auction.dto.ReqApiActDto;
import com.socket.auction.dto.ReqApiStatusDto;
import com.socket.auction.service.ApiService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/*
 * Process Flow : ApiController > ApiService > AuctionController > ResDtoService > RedisInfoService > AuctionService, RedissonService
 * ApiController : API 접속 분기
 * ApiService : Api 로직, 분기 > 로직별 경매 소켓 AuctionController 이동
 * AuctionController : 경매 Socket 상태별 분기
 * ResDtoService : Socket 통신에 사용할 데이터 DTO 생성, 수정
 * RedisInfoService : Socket 데이터에 사용할 Redis 데이터 삽입, 추출
 * AuctionService : 경매 로직 추출을 위한 메소드 집합
 * RedissonService : Redis 분삭 락 기능 - 입찰, 상태 변경시 사용
 */

@Controller
@RestController
@Api(description = " ", tags = "공구마켓 경매 API")
public class ApiController {

	@Autowired
	ApiService apiService;

	@ApiOperation(value = "[경매 상태 변경 API]")
	@RequestMapping(value = "/update/status", method = RequestMethod.POST)
	public Object apiUpdateStatus(@RequestParam(value="act_sno") int actSno, @RequestParam(value="act_stus_cd") int actStusCd, HttpServletRequest request) throws Exception {
		String service = getService(request);

		ReqApiStatusDto dto = new ReqApiStatusDto();
		dto.setAct_sno(actSno);
		dto.setAct_stus_cd(actStusCd);
		dto.setService(service);
		
		Object responseObject = null;
		responseObject = apiService.apiUpdateStatus(dto);
		return responseObject;
	}

	@ApiOperation(value = "[경매 변경 API]")
	@RequestMapping(value = "/update/act", method = RequestMethod.POST)
	public Object apiUpdateAct(@RequestParam(value="act_sno") int actSno, HttpServletRequest request) throws Exception {
		String service = getService(request);

		ReqApiActDto dto = new ReqApiActDto();
		dto.setAct_sno(actSno);
		dto.setService(service);

		Object responseObject = null;
		responseObject = apiService.apiUpdateAct(dto);
		return responseObject;
	}

	@ApiOperation(value = "[경매 변경 통합 API]")
	@RequestMapping(value = "/update/torder/act", method = RequestMethod.POST)
	public Object apiTorderUpdateAct(@RequestParam(value="tpo_idx") String tpoIdx, HttpServletRequest request) throws Exception {
		
		Object responseObject = null;
		responseObject = apiService.apiToderUpdateAct(tpoIdx);
		return responseObject;
	}

	public String getService(HttpServletRequest request) {
		String serviceUrl = request.getRequestURL().toString();

		String result = null;
		if(serviceUrl.contains("simsale")) {
			result = "jasonapp018";
		} else if(serviceUrl.contains("sale09")) {
			result = "jasonapp014";
		} else if(serviceUrl.contains("market09") || serviceUrl.contains("localhost")) {
			result = "jasonapp019";
		}
		
		return result;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * 변경 가능성 있기에 아래 API 삭제하지 않고 주석 처리
	 * 추후 오픈 시 사용하지 않을 경우 삭제해야 함. (관련 파일 모두)
	 */
	//
//	@ApiOperation(value = "[경매 입찰단위 변경 API]")
//	@RequestMapping(value = "/update/bid_unit", method = RequestMethod.POST)
//	public Object apiUpdateBidUnit(@RequestBody @Valid ReqeustApiBidUnitDto dto) throws Exception {
//		Object responseObject = null;
//		responseObject = apiService.apiUpdateBidUnit(dto);
//		return responseObject;
//	}
//
//	@ApiOperation(value = "[자동연장여부 변경 API(일반경매)]")
//	@RequestMapping(value = "/update/nrml_auto_extd", method = RequestMethod.POST)
//	public Object apiUpdateNrmlAutoExtd(@RequestBody @Valid ReqeustApiNrmlAutoExtdDto dto) throws Exception {
//		Object responseObject = null;
//		responseObject = apiService.updateNrmlAutoExtd(dto);
//		return responseObject;
//	}

}
