package com.socket.auction.repository.third.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.MmbrEntity;

@Repository("third.slave.MmbrThrdRepository")
public interface MmbrThrdRepository extends JpaRepository<MmbrEntity, Long>, MmbrThrdRepositoryCustom {
    public MmbrEntity findByMmbrId(String mmbrId);
    
}
