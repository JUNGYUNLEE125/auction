package com.socket.auction.dto;

import lombok.Data;

@Data
public class ResSocketBidInfoDto {
    private String mmbr_id;      // 입찰자 회원ID
    private String mmbr_nm;      // 입찰자 닉네임
    private double bid_amnt;     // 입찰가
    private String prfl_img;     // 프로필 이미지 URL
    private String reg_dtm;      // 등록일시
    private int    act_bid_sno;  // act_bid 시퀀스번호
    private String bid_rslt_cd;  // 입찰결과코드 01: 낙찰, 02: 낙찰 취소, 03: 낙찰 대기
}
