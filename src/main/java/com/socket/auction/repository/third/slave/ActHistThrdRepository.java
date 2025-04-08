package com.socket.auction.repository.third.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActHistEntity;

@Repository("third.slave.ActHistThrdRepository")
public interface ActHistThrdRepository extends JpaRepository<ActHistEntity, Long> {
    
}
