package com.socket.auction.repository.second.slave;

import org.springframework.stereotype.Repository;

import com.socket.auction.entity.MmbrEntity;

@Repository("second.slave.MmbrScndRepositoryCustom")
public interface MmbrScndRepositoryCustom {

    public MmbrEntity mmbrByMmbrId(String mmbrId);
}
