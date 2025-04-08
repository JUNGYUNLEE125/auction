package com.socket.auction.repository.third.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActHistEntity;

@Repository("third.master.ActHistMstrThrdRepository")
public interface ActHistMstrThrdRepository extends JpaRepository<ActHistEntity, Long> {
    
}
