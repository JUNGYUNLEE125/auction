package com.socket.auction.repository.third.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserInfmStatEntity;

@Repository("first.slave.UserInfmStatThrdRepository")
public interface UserInfmStatThrdRepository extends JpaRepository<UserInfmStatEntity, Long> {
    public UserInfmStatEntity findByUserSno(int userSno);    
}
