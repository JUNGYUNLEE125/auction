package com.socket.auction.entity;

import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="user")
public class UserEntity {
    @Id
    @GeneratedValue
    @Column(name = "user_sno")
    private int userSno;
  
    @Column(name = "mmbr_id")
    private String mmbrId;    
}
