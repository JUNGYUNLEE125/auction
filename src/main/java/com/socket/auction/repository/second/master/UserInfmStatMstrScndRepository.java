package com.socket.auction.repository.second.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserInfmStatEntity;

@Repository("second.master.UserInfmStatMstrScndRepository")
public interface UserInfmStatMstrScndRepository extends JpaRepository<UserInfmStatEntity, Long>, UserInfmStatMstrScndRepositoryCustom {
    public UserInfmStatEntity findByUserSno(int userSno);    
}
