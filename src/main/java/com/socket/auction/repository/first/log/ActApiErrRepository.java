package com.socket.auction.repository.first.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.log.ActApiErrEntity;

@Repository("first.log.ActApiErrRepository")
public interface ActApiErrRepository extends JpaRepository<ActApiErrEntity, Long> {

}
