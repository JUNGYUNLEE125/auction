package com.socket.auction.repository.first.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActHistEntity;

@Repository("first.slave.ActHistRepository")
public interface ActHistRepository extends JpaRepository<ActHistEntity, Long> {
    
}
