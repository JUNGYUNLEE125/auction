package com.socket.auction.repository.first.slave;

import com.socket.auction.dto.UserMmbrInfm;

import org.springframework.stereotype.Repository;

@Repository("first.slave.UserRepositoryCustom")
public interface UserRepositoryCustom {
    public UserMmbrInfm getUserMmbrInfm(String mmbrId);
}
