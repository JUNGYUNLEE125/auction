package com.socket.auction.repository.second.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserInfmStatEntity;

@Repository("second.slave.UserInfmStatScndRepository")
public interface UserInfmStatScndRepository extends JpaRepository<UserInfmStatEntity, Long> {
    public UserInfmStatEntity findByUserSno(int userSno);    
}
