package com.socket.auction.repository.second.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.log.ActApiErrEntity;

@Repository("second.log.ActApiErrScndRepository")
public interface ActApiErrScndRepository extends JpaRepository<ActApiErrEntity, Long> {

}
