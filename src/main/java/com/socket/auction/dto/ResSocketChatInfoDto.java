package com.socket.auction.dto;

import lombok.Data;

@Data
public class ResSocketChatInfoDto {
    private String chat_type;     // 채팅 타입
    private String chat_msg;      // 채팅 메세지
    private String mmbr_id;       // 입찰자 회원ID(시스템 메시지이면 NULL)
    private String mmbr_nm;       // 입찰자 닉네임(시스템 메시지이면 NULL)
    private double bid_amnt;      // 입찰가(시스템 메시지이면 NULL)
    private String prfl_img;      // 프로필 이미지 URL(시스템 메시지이면 NULL)
    private String frst_reg_dtm;  // 등록일시
}
