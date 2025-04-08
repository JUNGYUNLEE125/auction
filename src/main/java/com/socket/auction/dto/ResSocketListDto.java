package com.socket.auction.dto;

import java.util.List;

import lombok.Data;

@Data
public class ResSocketListDto {
    private int success;                  // 성공여부
    private ResSocketErrDto error;        // 에러정보
    private List<ResSocketDataDto> data;  // 리턴정보
}
