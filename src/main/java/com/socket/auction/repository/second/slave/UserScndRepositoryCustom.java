package com.socket.auction.repository.second.slave;

import com.socket.auction.dto.UserMmbrInfm;

import org.springframework.stereotype.Repository;

@Repository("second.slave.UserScndRepositoryCustom")
public interface UserScndRepositoryCustom {
    public UserMmbrInfm getUserMmbrInfm(String mmbrId);
}
