package com.socket.auction.repository.first.master;

import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserInfmStatEntity;

@Repository("first.master.UserInfmStatMstrRepositoryCustom")
public interface UserInfmStatMstrRepositoryCustom {
   
    public void updateBadgDspl(UserInfmStatEntity userInfmStatEntity);
    
}