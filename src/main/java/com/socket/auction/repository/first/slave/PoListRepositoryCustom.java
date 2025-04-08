package com.socket.auction.repository.first.slave;

import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;

@Repository("first.slave.UserRepositoryCustom")
public interface PoListRepositoryCustom {
    public ActEntity getTorderPoIdx(String tpoIdx);
}
