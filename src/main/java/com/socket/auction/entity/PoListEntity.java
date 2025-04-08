package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="po_list")
public class PoListEntity {
    @Id
    @GeneratedValue
    @Column(name = "po_idx")
    private int poIdx;
    
    @Column(name = "po_title")
    private String poTitle;

    @Column(name = "po_oprice")
    private int poOprice;

    @Column(name = "po_image")
    private String poImage;

    @Column(name = "po_image_sub")
    private String poImageSub;

    @Column(name = "torder_po_idx")
    private String torderPoIdx;
}
