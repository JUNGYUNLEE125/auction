package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name="act_hist")
public class ActHistEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "act_hist_sno")
  private int actHistSno;

  @Column(name = "chng_dtm")
  private String chngDtm;
  
  @Column(name = "act_sno")
  private int actSno;

  @Column(name = "po_idx")
  private int poIdx;

  @Column(name = "act_type_cd")
  private String actTypeCd;

  @Column(name = "act_stus_cd")
  private String actStusCd;

  @Column(name = "act_rslt_cd")
  private String actRsltCd;

  @Column(name = "act_sdtm")
  private String actSdtm;

  @Column(name = "act_edtm")
  private String actEdtm;

  @Column(name = "bid_unit")
  private int bidUnit;

  @Column(name = "max_bid_unit")
  private int maxBidUnit;

  @Column(name = "bid_strt_amnt")
  private int bidStrtAmnt;

  @Column(name = "bid_curr_max_amnt")
  private int bidCurrMaxAmnt;

  @Column(name = "bid_finl_amnt")
  private int bidFinlAmnt;

  @Column(name = "bid_cnt")
  private int bidCnt;

  @Column(name = "bidr_cnt")
  private int bidrCnt;

  @Column(name = "sucs_bidr_set_cnt")
  private int sucsBidrSetCnt;

  @Column(name = "wait_bidr_set_cnt")
  private int waitBidrSetCnt;

  @Column(name = "auto_extd_use_yn")
  private String autoExtdUseYn;

  @Column(name = "live_intl_yn")
  private String liveIntlYn;

  @Column(name = "sucs_bid_yn")
  private String sucsBidYn;

  @Column(name = "del_yn")
  private String delYn;

  @Column(name = "frst_rgsr_id")
  private String frstRgsrId;

  @Column(name = "frst_reg_dtm")
  private String frstRegDtm;

  @Column(name = "finl_edtr_id")
  private String finlEdtrId;

  @Column(name = "finl_edit_dtm")
  private String finlEditDtm;

  @Column(name = "finl_edtr_ns")
  private String finlEdtrNs;
    
}