package com.socket.auction.repository.first.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.MmbrPntEntity;

@Repository("first.slave.MmbrPntRepository")
public interface MmbrPntRepository extends JpaRepository<MmbrPntEntity, Long> {
    public MmbrPntEntity findByMmbrIdAndPntStusCd(String mmbrId, String PntStusCd);    
}