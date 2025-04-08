package com.socket.auction.repository.third.slave;

import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;

@Repository("third.slave.UserRepositoryCustom")
public interface PoListThrdRepositoryCustom {
    public ActEntity getTorderPoIdx(String tpoIdx);
}
