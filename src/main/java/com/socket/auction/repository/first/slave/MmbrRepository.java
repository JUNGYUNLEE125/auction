package com.socket.auction.repository.first.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.MmbrEntity;

@Repository("first.slave.MmbrRepository")
public interface MmbrRepository extends JpaRepository<MmbrEntity, Long> {
    public MmbrEntity findByMmbrId(String mmbrId);
    
}
