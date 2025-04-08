package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name="act_bid")
public class ActBidEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "act_bid_sno")
    private int actBidSno;

    @Column(name = "act_sno")
    private int actSno;

    @Column(name = "po_idx")
    private int poIdx;

    @Column(name = "mmbr_id")
    private String mmbrId;

    @Column(name = "bid_amnt")
    private int bidAmnt;

    @Column(name = "bid_dtm")
    private String bidDtm;

    @Column(name = "bid_rslt_cd")
    private String bidRsltCd;

    @Column(name = "wait_bidr_seq")
    private String waitBidrSeq;

    @Column(name = "sucs_bid_dtm")
    private String sucsBidDtm;

    @Column(name = "pay_dtm")
    private String payDtm;

    @Column(name = "pay_psbl_dtm")
    private String payPsblDtm;

    @Column(name = "ordr_no")
    private String ordrNo;

    @Column(name = "ordn_no")
    private String ordnNo;

    @Column(name = "frst_reg_dtm")
    private String frstRedDtm;

    @Column(name = "finl_edit_dtm")
    private String finlEditDtm;

    @Column(name = "finl_edtr_ns")
    private String finlEdtrNs;
    
}