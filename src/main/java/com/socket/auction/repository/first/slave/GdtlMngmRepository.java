package com.socket.auction.repository.first.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.GdtlMngmEntity;

@Repository("first.slave.GdtlMngmRepository")
public interface GdtlMngmRepository extends JpaRepository<GdtlMngmEntity, Long> {
    public GdtlMngmEntity findByPoIdx(int poIdx);
    
}