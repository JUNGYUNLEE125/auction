package com.socket.auction.dto;

import lombok.Data;

@Data
public class ReqSocketDto {
    private String request;       // 요청 형식(auction)
    private String type;          // 구분(aos/ios/web)
    private String service;       // 서비스 DB명칭 할인중독:jasonapp014, 심쿵할인:jasonapp018, 공구마켓:jasonapp019
    private int    act_sno;       // 경매일련번호
    private String msg;           // init : 소켓 접속시 요청, more : 실시간 입찰현황 > 더보기 (100개 리턴), bid : 입찰등록
    private String mmbr_id;       // 회원ID
    private String mmbr_nm;       // 회원이름    
    private String prfl_img;      // 프로필 이미지 URL
    private int    bid_amnt;      // 입찰가
    private int    list_cnt;      // 리스트 개수
    private int    live_cnt;      // 라이브채팅 갯수
    private int    game_seq;      // 게임미션 시퀀스
    private String jsn_auth;      // 로그인 토큰
    private String frst_reg_dtm;  // 등록날짜
}
