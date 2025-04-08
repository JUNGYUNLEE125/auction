package com.socket.auction.repository.first.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActHistEntity;

@Repository("first.master.ActHistMstrRepository")
public interface ActHistMstrRepository extends JpaRepository<ActHistEntity, Long> {
    
}
