package com.socket.auction.dto;

import lombok.Data;

@Data
public class ResSocketDto {
    private int success;            // 성공여부
    private ResSocketErrDto error;  // 에러정보
    private String stus_type;       // 상태타입
    private ResSocketDataDto data;  // 리턴정보
}
