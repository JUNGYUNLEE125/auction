package com.socket.auction.repository.second.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActSetEntity;

@Repository("second.slave.ActSetScndRepository")
public interface ActSetScndRepository extends JpaRepository<ActSetEntity, Long> {
    public ActSetEntity findByActSetSno(int actSetSno);
}
