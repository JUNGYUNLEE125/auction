package com.socket.auction.repository.first.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserInfmStatEntity;

@Repository("first.slave.UserInfmStatRepository")
public interface UserInfmStatRepository extends JpaRepository<UserInfmStatEntity, Long> {
    public UserInfmStatEntity findByUserSno(int userSno);    
}
