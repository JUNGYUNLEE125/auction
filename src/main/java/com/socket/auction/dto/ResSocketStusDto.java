package com.socket.auction.dto;

import lombok.Data;

@Data
public class ResSocketStusDto {
    private String act_stus_cd;         // 경매상태코드 01: 진행예정, 02: 진행중, 03: 종료
    private String act_rslt_cd;         // 경매결과코드 01: 낙찰, 02: 유찰
    private double max_bid;             // 현재최고가
    private double my_bid;              // 나의입찰가
    private String my_bid_dtm;           // 나의입찰시간
    private int    bid_cnt;             // 입찰수
    private int    bidr_cnt;            // 입찰자수
    private String frst_bidr_yn = "N";  // 처음 입찰자 유무(Y/N)
}
