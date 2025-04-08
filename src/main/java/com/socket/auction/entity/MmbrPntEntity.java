package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="mmbr_pnt")
public class MmbrPntEntity {
    @Id
    @GeneratedValue
    @Column(name = "mmbr_act_pnt_sno")
    private int mmbrActPntSno;
  
    @Column(name = "mmbr_id")
    private String mmbrId ;
  
    @Column(name = "pnt_stus_cd")
    private String pntStusCd ;
  
    @Column(name = "pnt_rond")
    private int pntRond ;
  
    @Column(name = "pnt_resn")
    private String pntResn ;
  
    @Column(name = "pnt_cntn")
    private String pntCntn ;
  
    @Column(name = "aply_strt_dtm")
    private String aplyStrtDtm ;
  
    @Column(name = "aply_end_dtm")
    private String aplyEndDtm ;
  
    @Column(name = "prcs_dtm")
    private String prcsDtm ;
  
    @Column(name = "rst_prcs_admn_id")
    private String rstPrcsAdmnId ;
}
