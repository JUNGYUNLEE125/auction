package com.socket.auction.repository.second.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActHistEntity;

@Repository("second.slave.ActHistScndRepository")
public interface ActHistScndRepository extends JpaRepository<ActHistEntity, Long> {
    
}
