package com.socket.auction.repository.third.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.log.ActApiErrEntity;

@Repository("third.log.ActApiErrThrdRepository")
public interface ActApiErrThrdRepository extends JpaRepository<ActApiErrEntity, Long> {

}
