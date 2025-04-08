package com.socket.auction.repository.third.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.GdtlMngmEntity;

@Repository("third.slave.GdtlMngmThrdRepository")
public interface GdtlMngmThrdRepository extends JpaRepository<GdtlMngmEntity, Long> {
    public GdtlMngmEntity findByPoIdx(int poIdx);
    
}