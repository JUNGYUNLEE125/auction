package com.socket.auction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socket.auction.common.CommonConstants;
import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResApiDto;
import com.socket.auction.dto.log.ReqActApiErrDto;
import com.socket.auction.entity.log.ActApiErrEntity;
import com.socket.auction.repository.first.log.ActApiErrRepository;
import com.socket.auction.utils.RepositoryUtil;

@Service
public class ApiCommonService {

	@Autowired
	ActApiErrRepository actApiErrRepository;

	@Autowired
	RepositoryUtil repositoryUtil;

	/**
	 * 경매API 에러 이력 DB 저장
	 * 
	 * @param errDto
	 * @return
	 */
	@Transactional
	public ResApiDto saveApiError(ReqActApiErrDto errDto) {

		ActApiErrEntity apiErrEntity = new ActApiErrEntity();
		apiErrEntity.setLogType(errDto.getLogType());
		apiErrEntity.setRqstLog(errDto.getRqstLog());
		apiErrEntity.setRspnsLog(errDto.getRspnsLog());

		actApiErrRepository.save(apiErrEntity);

		ResApiDto apiDto = new ResApiDto();
		apiDto.setResult(CommonConstants.DEFAULT_ERROR);
		apiDto.setMessage(errDto.getRspnsLog());

		return apiDto;
	}
	 
	@Transactional
	public ResApiDto saveApiError(ReqSocketDto reqSocketDto, ReqActApiErrDto errDto) {

		ActApiErrEntity apiErrEntity = new ActApiErrEntity();
		apiErrEntity.setLogType(errDto.getLogType());
		apiErrEntity.setRqstLog(errDto.getRqstLog());
		apiErrEntity.setRspnsLog(errDto.getRspnsLog());

		repositoryUtil.saveApiError(reqSocketDto, apiErrEntity);

		ResApiDto apiDto = new ResApiDto();
		apiDto.setResult(CommonConstants.DEFAULT_ERROR);
		apiDto.setMessage(errDto.getRspnsLog());

		return apiDto;
	}

	public ResApiDto actGodsNotExist(ReqSocketDto reqSocketDto, String message, ReqActApiErrDto errDto) {
		errDto.setRspnsLog(CommonConstants.MESSAGE_ACT_GODS_NOT_EXIST + message);
		return saveApiError(reqSocketDto, errDto);
	}

	public ResApiDto actGodsNotExistStus(ReqSocketDto reqSocketDto, String message, ReqActApiErrDto errDto) {
		errDto.setRspnsLog(message);
		return saveApiError(reqSocketDto, errDto);
	}

	public ResApiDto serviceError(ReqSocketDto reqSocketDto, String message, ReqActApiErrDto errDto) {
		errDto.setRspnsLog(message);
		return saveApiError(reqSocketDto, errDto);
	}
}
