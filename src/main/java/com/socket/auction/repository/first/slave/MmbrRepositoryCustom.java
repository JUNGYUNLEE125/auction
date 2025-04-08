package com.socket.auction.repository.first.slave;

import org.springframework.stereotype.Repository;

import com.socket.auction.entity.MmbrEntity;

@Repository("first.slave.MmbrRepositoryCustom")
public interface MmbrRepositoryCustom {

    public MmbrEntity mmbrByMmbrId(String mmbrId);
}
