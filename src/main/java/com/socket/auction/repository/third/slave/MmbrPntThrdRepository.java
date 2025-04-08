package com.socket.auction.repository.third.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.MmbrPntEntity;

@Repository("third.slave.MmbrPntThrdRepository")
public interface MmbrPntThrdRepository extends JpaRepository<MmbrPntEntity, Long> {
    public MmbrPntEntity findByMmbrIdAndPntStusCd(String mmbrId, String PntStusCd);    
}