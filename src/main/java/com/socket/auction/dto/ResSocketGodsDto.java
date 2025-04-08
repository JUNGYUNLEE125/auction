package com.socket.auction.dto;

import lombok.Data;

@Data
public class ResSocketGodsDto {
    private int    po_idx;     // 경매상품번호
    private String po_title;   // 상품명
    private int    po_oprice;  // 권장소비자가
    private String po_image;   // 상품이미지
    private String po_image_sub;   // 상품서브이미지
}
