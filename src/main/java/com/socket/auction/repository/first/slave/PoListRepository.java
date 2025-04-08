package com.socket.auction.repository.first.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.PoListEntity;

@Repository("first.slave.PoListRepository")
public interface PoListRepository extends JpaRepository<PoListEntity, Long>, PoListRepositoryCustom {
    public PoListEntity findByPoIdx(int poIdx);
    
}