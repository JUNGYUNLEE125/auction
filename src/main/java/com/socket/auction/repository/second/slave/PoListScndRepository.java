package com.socket.auction.repository.second.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.PoListEntity;

@Repository("second.slave.PoListScndRepository")
public interface PoListScndRepository extends JpaRepository<PoListEntity, Long>, PoListScndRepositoryCustom {
    public PoListEntity findByPoIdx(int poIdx);
    
}