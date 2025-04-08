package com.socket.auction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.socket.auction.common.CommonConstants;
import com.socket.auction.controller.AuctionController;
import com.socket.auction.dto.ReqApiActDto;
import com.socket.auction.dto.ReqApiStatusDto;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResApiDto;
import com.socket.auction.dto.log.ReqActApiErrDto;
import com.socket.auction.entity.ActEntity;
import com.socket.auction.utils.RepositoryUtil;

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

@Service
public class ApiService {

	@Autowired
	RepositoryUtil repositoryUtil;

	@Autowired
	AuctionController auctionController;

	@Autowired
	ApiCommonService apiCommonService;

	@Autowired
	RedissonService redissonService;

    private Logger logger = LoggerFactory.getLogger(ApiService.class);

	@Transactional
	public ResApiDto apiUpdateStatus(ReqApiStatusDto dto) {		
		ReqSocketDto reqSocketDto = new ReqSocketDto();
		reqSocketDto.setService(dto.getService());	
		reqSocketDto.setAct_sno(dto.getAct_sno());

		ResApiDto     apiDto = new ResApiDto();
		ReqActApiErrDto errDto = new ReqActApiErrDto();
		
		Gson gson = new Gson();
		errDto.setLogType("1"); // 경매 상태 변경
		errDto.setRqstLog(gson.toJson(dto));

		try {
			int count = 0;
			ActEntity actEntity = repositoryUtil.actByActSno(reqSocketDto);
			String actStusCd = actEntity.getActStusCd();
			
			count = repositoryUtil.actCountActSno(reqSocketDto);

			String messageStr = null;
			if(dto.getAct_stus_cd() == 1){
				if(!"01".equals(actStusCd)) {
					messageStr = "현재 경매 예정 상태가 아닙니다. [actStusCd] 입력된 값: [" + actStusCd + "]";
					apiDto = apiCommonService.actGodsNotExistStus(reqSocketDto, messageStr, errDto);
				}
			} else if(dto.getAct_stus_cd()==2 || dto.getAct_stus_cd()==3 || dto.getAct_stus_cd()==4 || dto.getAct_stus_cd()==5){
				if(!"02".equals(actStusCd)) {
					messageStr = "현재 경매 진행 상태 아닙니다. [actStusCd] 입력된 값: [" + actStusCd + "]";
					apiDto = apiCommonService.actGodsNotExistStus(reqSocketDto, messageStr, errDto);
				}
			} else if(count == 0) {
				// 데이터 존재하지 않을 경우. api error 테이블 저장
				messageStr = " [act_sno] 입력된 값: [" + dto.getAct_sno() + "]";
				apiDto = apiCommonService.actGodsNotExist(reqSocketDto, messageStr, errDto);
			} 
			
			if(messageStr == null) {
				// socket 호출
				// auctionController.updateStatus(reqSocketDto, dto.getAct_stus_cd());
				messageStr = redissonService.updateStatus(reqSocketDto, dto.getAct_stus_cd(), "api");

				// 성공일 경우 ok 메시지
				apiDto.setResult(CommonConstants.DEFAULT_SUCCESS);
				apiDto.setMessage(null);
			}
		} catch (Exception e) {
			apiDto = apiCommonService.serviceError(reqSocketDto, e.getMessage(), errDto);
		}

		return apiDto;
	}

	@Transactional
	public ResApiDto apiUpdateAct(ReqApiActDto dto) {
		ReqSocketDto reqSocketDto = new ReqSocketDto();
		reqSocketDto.setService(dto.getService());	
		reqSocketDto.setAct_sno(dto.getAct_sno());

		ResApiDto apiDto = new ResApiDto();
		ReqActApiErrDto errDto = new ReqActApiErrDto();
		Gson gson = new Gson();
		errDto.setLogType("2"); // 경매 변경
		errDto.setRqstLog(gson.toJson(dto));
		errDto.setRspnsLog("null");

		String messageStr = null;
		try {
			int count = 0;
			ActEntity actEntity = repositoryUtil.actByActSno(reqSocketDto);
			String actStusCd = actEntity.getActStusCd();

			count = repositoryUtil.actCountActSno(reqSocketDto);

			if("03".equals(actStusCd)) {
				messageStr = "현재 경매 종료 상태 입니다. [actStusCd] 입력된 값: [" + actStusCd + "]";
				apiDto = apiCommonService.actGodsNotExistStus(reqSocketDto, messageStr, errDto);
			} else if (count == 0) {
				// 데이터 존재하지 않을 경우. api error 테이블 저장
				messageStr = " [act_sno] 입력된 값: [" + dto.getAct_sno() + "]";
				apiDto = apiCommonService.actGodsNotExist(reqSocketDto, messageStr, errDto);
			}
			
			if(messageStr == null) {
				// socket 호출
				auctionController.updateAct(reqSocketDto);

				// 성공일 경우 ok 메시지
				apiDto.setResult(CommonConstants.DEFAULT_SUCCESS);
				apiDto.setMessage(null);
			}
		} catch (Exception e) {
			apiDto = apiCommonService.serviceError(reqSocketDto, e.getMessage(), errDto);
		}

		return apiDto;
	}

	public ResApiDto apiToderUpdateAct(String tpoIdx) {
		String message = null;

		ResApiDto apiTotalDto = new ResApiDto();
		apiTotalDto.setResult("success");

		try {
			ActEntity actEntity = repositoryUtil.getTorderPoIdx019(tpoIdx);
			if(actEntity != null) {
				ReqApiActDto dto = new ReqApiActDto();
				dto.setAct_sno(actEntity.getActSno());
				dto.setService("jasonapp019");

				
				ResApiDto apiDto = apiUpdateAct(dto);

				if(apiDto.getMessage() == null) {
					message = "[MARKET09 SUCCESS]";
				} else {
					message = "[MARKET09 ("+ tpoIdx +")] : "+ apiDto.getMessage();
				}
			}
		} catch(Exception e){
			apiTotalDto.setResult("error");			
			message = "통합상품관리 수정 API 에러(jasonapp019) : "+ tpoIdx;
			logger.info(message + e);
		}
		
		try {
			ActEntity actScndEntity = repositoryUtil.getTorderPoIdx018(tpoIdx);
			if(actScndEntity != null) {
				ReqApiActDto dto = new ReqApiActDto();
				dto.setAct_sno(actScndEntity.getActSno());
				dto.setService("jasonapp018");

				ResApiDto apiDto = apiUpdateAct(dto);

				if(apiDto.getMessage() == null) {
					message = message + ", [SIMSALE SUCCESS]";
				} else {
					message = message + ", [SIMSALE ("+ tpoIdx +")] : "+ apiDto.getMessage();
				}
			}
		} catch(Exception e) {
			apiTotalDto.setResult("error");	
			message = message + "\n" + "통합상품관리 수정 API 에러(jasonapp018) : "+ tpoIdx;
			logger.info(message + e);
		}
		
		try {
			ActEntity actThrEntity = repositoryUtil.getTorderPoIdx014(tpoIdx);
			if(actThrEntity != null) {
				ReqApiActDto dto = new ReqApiActDto();
				dto.setAct_sno(actThrEntity.getActSno());
				dto.setService("jasonapp014");

				ResApiDto apiDto = apiUpdateAct(dto);

				if(apiDto.getMessage() == null) {
					message = message +  ", [SALE09 SUCCESS]";
				} else {
					message = message +  ", [SALE09 ("+ tpoIdx +")] : "+ apiDto.getMessage();
				}
			}
		} catch(Exception e) {
			apiTotalDto.setResult("error");	
			message = message + "\n" + "통합상품관리 수정 API 에러(jasonapp014) : "+ tpoIdx;
			logger.info(message + e);
		}		

		apiTotalDto.setMessage(message);
		return apiTotalDto;
	}

//	@Transactional
//	public ResponseApiDto apiUpdateBidUnit(ReqeustApiBidUnitDto dto) {
//		ResponseApiDto apiDto = new ResponseApiDto();
//		ReqeustZuctnErrDto errDto = new ReqeustZuctnErrDto();
//		Gson gson = new Gson();
//		errDto.setLogType("3"); // 경매 입찰 단위 변경
//		errDto.setRqstLog(gson.toJson(dto));
//
//		int count = 0;
//		try {
//			count = actRepository.countByActSno(dto.getAct_sno());
//
//			if (count == 0) {
//				// 데이터 존재하지 않을 경우. api error 테이블 저장
//				String messageStr = " [act_sno] 입력된 값: [" + dto.getAct_sno() + "]";
//				apiDto = apiCommonService.actGodsNotExist(messageStr, errDto);
//			} else {
//				// socket 호출 (임시 주석)
////				socketService.updateBidUnit(dto.getBid_unit());
//
//				// 성공일 경우 ok 메시지
//				apiDto.setResult(CommonConstants.DEFAULT_SUCCESS);
//				apiDto.setMessage(null);
//			}
//		} catch (Exception e) {
//			apiDto = apiCommonService.serviceError(e.getMessage(), errDto);
//		}
//
//		return apiDto;
//	}
//
//	@Transactional
//	public ResponseApiDto updateNrmlAutoExtd(ReqeustApiNrmlAutoExtdDto dto) throws Exception {
//		ResponseApiDto apiDto = new ResponseApiDto();
//		ReqeustZuctnErrDto errDto = new ReqeustZuctnErrDto();
//		Gson gson = new Gson();
//		errDto.setLogType("4"); // 자동연장여부 변경 API
//		errDto.setRqstLog(gson.toJson(dto));
//
//		// auto_extd_yn 값이 Y/N인지 체크
//		String autoExtdYn = dto.getAuto_extd_use_yn().trim().toLowerCase();
//		if (!autoExtdYn.equals("y") && !autoExtdYn.equals("n")) {
//			String messageStr = "[auto_extd_use_yn](은)는 Y/N 입력해야 합니다. 입력된 값: [" + dto.getAuto_extd_use_yn() + "]";
//			apiDto = apiCommonService.serviceError(messageStr, errDto);
//		} else {
//			int count = 0;
//			try {
//				count = actRepository.countByActSno(dto.getAct_sno());
//
//				if (count == 0) {
//					// 데이터 존재하지 않을 경우. api error 테이블 저장
//					String messageStr = " [act_sno] 입력된 값: [" + dto.getAct_sno() + "]";
//					apiDto = apiCommonService.actGodsNotExist(messageStr, errDto);
//				} else {
//					// socket 호출 (임시 주석)
////					socketService.updateNrmlAutoExtd();
//
//					// 성공일 경우 ok 메시지
//					apiDto.setResult(CommonConstants.DEFAULT_SUCCESS);
//					apiDto.setMessage(null);
//				}
//			} catch (Exception e) {
//				apiDto = apiCommonService.serviceError(e.getMessage(), errDto);
//			}
//		}
//
//		return apiDto;
//	}

}
