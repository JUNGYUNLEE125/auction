package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="user_infm_stat")
public class UserInfmStatEntity {    
    @Id
    @GeneratedValue
    @Column(name = "user_sno")
    private int userSno;
  
    @Column(name = "myact_badg_dspl_yn")
    private String myactBadgDsplYn;
}