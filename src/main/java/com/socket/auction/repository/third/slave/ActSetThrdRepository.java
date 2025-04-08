package com.socket.auction.repository.third.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActSetEntity;

@Repository("third.slave.ActSetRepository")
public interface ActSetThrdRepository extends JpaRepository<ActSetEntity, Long> {
    public ActSetEntity findByActSetSno(int actSetSno);
}
