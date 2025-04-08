package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="gdtl_mngm")
public class GdtlMngmEntity {
    @Id
    @GeneratedValue
    @Column(name = "gdtl_sno")
    private int gdtlSno;
  
    @Column(name = "po_idx")
    private int poIdx;

    @Column(name = "po_list_option")
    private String poListOption;

    @Column(name = "act_strt_price")
    private int actStrtPrice;

    @Column(name = "act_min_use_yn")
    private String actMinUseYn;

    @Column(name = "act_min_price")
    private int actMinPrice;

    @Column(name = "act_max_use_yn")
    private String actMaxUseYn;

    @Column(name = "act_max_price")
    private int actMaxPrice;
}
