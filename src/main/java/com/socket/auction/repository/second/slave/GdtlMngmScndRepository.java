package com.socket.auction.repository.second.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.GdtlMngmEntity;

@Repository("second.slave.GdtlMngmScndRepository")
public interface GdtlMngmScndRepository extends JpaRepository<GdtlMngmEntity, Long> {
    public GdtlMngmEntity findByPoIdx(int poIdx);
    
}