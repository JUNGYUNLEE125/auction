package com.socket.auction.repository.first.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActSetEntity;

@Repository("first.slave.ActSetRepository")
public interface ActSetRepository extends JpaRepository<ActSetEntity, Long> {
    public ActSetEntity findByActSetSno(int actSetSno);
}
