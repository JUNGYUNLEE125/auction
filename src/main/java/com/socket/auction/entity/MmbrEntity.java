package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="mmbr")
public class MmbrEntity {
    @Id
    @GeneratedValue
    @Column(name = "mmbr_id")
    private String mmbrId;
  
    @Column(name = "mmbr_nm")
    private String mmbrNm;

    @Column(name = "mmbr_tlno")
    private String mmbrTlno;
  
    @Column(name = "prfl_img")
    private String prflImg;

    @Column(name = "mmbr_stus_cd")
    private String mmbrStusCd;

    @Column(name = "act_pnt_yn")
    private String actPntYn;    
    
}
