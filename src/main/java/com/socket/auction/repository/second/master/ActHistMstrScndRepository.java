package com.socket.auction.repository.second.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActHistEntity;

@Repository("second.master.ActHistMstrScndRepository")
public interface ActHistMstrScndRepository extends JpaRepository<ActHistEntity, Long> {
    
}
