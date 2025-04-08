package com.socket.auction.repository.third.slave;

import org.springframework.stereotype.Repository;

import com.socket.auction.entity.MmbrEntity;

@Repository("third.slave.MmbrThrdRepositoryCustom")
public interface MmbrThrdRepositoryCustom {

    public MmbrEntity mmbrByMmbrId(String mmbrId);
}
