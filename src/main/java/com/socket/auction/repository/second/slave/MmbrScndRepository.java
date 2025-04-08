package com.socket.auction.repository.second.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.MmbrEntity;

@Repository("second.slave.MmbrScndRepository")
public interface MmbrScndRepository extends JpaRepository<MmbrEntity, Long>, MmbrScndRepositoryCustom {
    public MmbrEntity findByMmbrId(String mmbrId);
    
}
