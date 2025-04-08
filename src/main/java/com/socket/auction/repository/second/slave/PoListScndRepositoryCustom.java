package com.socket.auction.repository.second.slave;

import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;

@Repository("second.slave.PoListScndRepositoryCustom")
public interface PoListScndRepositoryCustom {
    public ActEntity getTorderPoIdx(String tpoIdx);
}
