package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="act_set")
public class ActSetEntity {
    @Id
    @GeneratedValue
    @Column(name = "act_set_sno")
    private int actSetSno;
  
    @Column(name = "pay_expr_cd")
    private String payExprCd;    
}
