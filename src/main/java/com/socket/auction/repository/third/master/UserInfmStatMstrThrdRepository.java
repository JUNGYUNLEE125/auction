package com.socket.auction.repository.third.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserInfmStatEntity;

@Repository("third.master.UserInfmStatMstrThrdRepository")
public interface UserInfmStatMstrThrdRepository extends JpaRepository<UserInfmStatEntity, Long>, UserInfmStatMstrThrdRepositoryCustom {
    public UserInfmStatEntity findByUserSno(int userSno);    
}
