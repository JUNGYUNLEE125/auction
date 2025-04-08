package com.socket.auction.repository.third.slave;

import com.socket.auction.dto.UserMmbrInfm;

import org.springframework.stereotype.Repository;

@Repository("second.slave.UserThrdRepositoryCustom")
public interface UserThrdRepositoryCustom {
    public UserMmbrInfm getUserMmbrInfm(String mmbrId);
}
