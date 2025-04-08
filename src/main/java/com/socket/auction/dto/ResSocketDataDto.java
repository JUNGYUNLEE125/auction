package com.socket.auction.dto;

import java.util.List;

import lombok.Data;

@Data
public class ResSocketDataDto {
    private ResSocketGodsDto gods_info;            // 상품정보
    private ResSocketActInfoDto act_info;          // 경매정보
    private ResSocketStusDto act_stus;             // 상태정보
    private List<ResSocketBidInfoDto> bid_info;    // 입찰정보
    private List<ResSocketChatInfoDto> chat_info;  // 채팅정보
}
