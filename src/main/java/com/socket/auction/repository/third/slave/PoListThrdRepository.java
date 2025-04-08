package com.socket.auction.repository.third.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.PoListEntity;

@Repository("third.slave.PoListRepository")
public interface PoListThrdRepository extends JpaRepository<PoListEntity, Long>, PoListThrdRepositoryCustom {
    public PoListEntity findByPoIdx(int poIdx);
    
}