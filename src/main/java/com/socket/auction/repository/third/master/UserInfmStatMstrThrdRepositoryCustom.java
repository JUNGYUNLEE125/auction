package com.socket.auction.repository.third.master;

import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserInfmStatEntity;

@Repository("third.master.UserInfmStatMstrThrdRepositoryCustom")
public interface UserInfmStatMstrThrdRepositoryCustom {
   
    public void updateBadgDspl(UserInfmStatEntity userInfmStatEntity);
    
}