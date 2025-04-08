package com.socket.auction.dto;

import lombok.Data;

@Data
public class ResSocketActInfoDto {
    private int    act_sno;            // 경매일련번호
    private String act_type_cd;        // 경매종류 : 01: 일반, 02: 라이브
    private String act_stus_cd;        // 경매상태코드 01: 진행예정, 02: 진행중, 03: 종료
    private String act_rslt_cd;        // 경매결과코드 01: 낙찰, 02: 유찰
    private int    bid_unit;           // 경매입찰단위
    private int    max_bid_unit;       // 최대입찰단위
    private String act_sdtm;           // 경매시작일시
    private String act_edtm;           // 경매종료일시
    private String po_list_option;      // 옵션등록여부
    private int    act_strt_price;     // 경매시작가
    private String act_min_use_yn;     // 최저 낙찰가 사용여부(Y/N)
    private int    act_min_price;      // 최소낙찰가
    private String act_max_use_yn;     // 최고 낙찰가 사용여부(Y/N)
    private int    act_max_price;      // 최고낙찰가
    private int    sucs_bidr_set_cnt;  // 낙찰자 수
    private int    wait_bidr_set_cnt;  // 낙찰 대기자 수
    private String auto_extd_use_yn;   // 자동마감연장 사용여부(Y/N)
    private int    act_fnsh_cnt;       // 라이브 마감카운팅(초)
    private int    act_recnt_intrvl;   // 라이브 재카운팅 간격(초)
    private String act_exps_yn;        // 입찰창 노출 여부(Y/N)
}
