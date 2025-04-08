package com.socket.auction.repository.second.master;

import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserInfmStatEntity;

@Repository("second.master.UserInfmStatMstrScndRepositoryCustom")
public interface UserInfmStatMstrScndRepositoryCustom {
   
    public void updateBadgDspl(UserInfmStatEntity userInfmStatEntity);
    
}