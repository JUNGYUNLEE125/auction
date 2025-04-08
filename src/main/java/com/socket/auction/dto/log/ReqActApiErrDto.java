package com.socket.auction.dto.log;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReqActApiErrDto {
	private String logType; // 로그유형
	private String rqstLog; // 요청내용
	private String rspnsLog = "emp"; // 반환내용

}
