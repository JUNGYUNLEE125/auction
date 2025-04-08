package com.socket.auction.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.google.gson.Gson;
import com.socket.auction.dto.log.ReqActApiErrDto;
import com.socket.auction.service.ApiCommonService;

@RestControllerAdvice
public class CommonExceptionHandler {

	@Autowired
	ApiCommonService apiCommonService;

	/**
	 * 유효성 검사했을 때 발생한 에러
	 * 
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Object processValidationError(MethodArgumentNotValidException exception) {
		Object responseObject = null;
		ReqActApiErrDto errDto = new ReqActApiErrDto();
		Gson gson = new Gson();

		// 에러 내용
		StringBuilder builder = new StringBuilder();
		exception.getBindingResult().getAllErrors().forEach((err) -> {
			builder.append("[");
			builder.append(((FieldError) err).getField());
			builder.append("](은)는 ");
			builder.append(err.getDefaultMessage());
			builder.append(" 입력된 값: [");
			builder.append(((FieldError) err).getRejectedValue());
			builder.append("]");
		});

		// 로그유형 저장하기위해 Method 명으로 구분
		String methodName = exception.getParameter().getMethod().getName();
		if (methodName.indexOf("apiUpdateStatus") > -1) {
			errDto.setLogType("1"); // 상태변경
		} else if (methodName.indexOf("apiUpdateAct") > -1) {
			errDto.setLogType("2"); // 경매변경
//		} else if (methodName.indexOf("apiUpdateBidUnit") > -1) {
//			errDto.setLogType("3"); // 입찰단위변경
//		} else if (methodName.indexOf("apiUpdateNrmlAutoExtd") > -1) {
//			errDto.setLogType("4"); // 자동마감연장변경
		} else {
			errDto.setLogType("0");
		}

		errDto.setRqstLog(gson.toJson(exception.getBindingResult().getTarget())); // 요청내용
		errDto.setRspnsLog(builder.toString()); // 반환내용

		// Error DB 저장
		responseObject = apiCommonService.saveApiError(errDto);
		return responseObject;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Object HttpMessageNotReadableError(HttpMessageNotReadableException exception) {
		Object responseObject = null;
		ReqActApiErrDto errDto = new ReqActApiErrDto();

		// 로그유형 저장하기위해 Method 명으로 구분
		String methodName = exception.getMessage();
		if (methodName.contains("ReqeustApiStatusDto")) {
			errDto.setLogType("1"); // 상태변경
		} else if (methodName.contains("ReqeustApiActDto")) {
			errDto.setLogType("2"); // 경매변경
//		} else if (methodName.contains("ReqeustApiBidUnitDto")) {
//			errDto.setLogType("3"); // 입찰단위변경
//		} else if (methodName.contains("ReqeustApiNrmlAutoExtdDto")) {
//			errDto.setLogType("4"); // 자동마감연장변경
		} else {
			errDto.setLogType("0");
		}

		errDto.setRqstLog(exception.getMessage());
		errDto.setRspnsLog(exception.getCause().getMessage());

		responseObject = apiCommonService.saveApiError(errDto);

		return responseObject;
	}
}
