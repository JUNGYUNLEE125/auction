package com.socket.auction.dto;

import lombok.Data;

@Data
public class ResSocketErrDto {
    private String code;    // 에러코드
    private String reason;  // 에러사유
}
