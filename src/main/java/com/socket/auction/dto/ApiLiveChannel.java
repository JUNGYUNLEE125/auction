package com.socket.auction.dto;

import lombok.Data;

@Data
public class ApiLiveChannel {
    private int auctionFinishCount;      // 라이브 마감카운팅(초)
    private int auctionRecountInterval;  // 라이브 재카운팅 간격(초)
}
