package com.socket.auction.repository.third.master;

import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("third.master.ActMstrThrdRepositoryCustom")
public interface ActMstrThrdRepositoryCustom {
    public ActEntity getAct(int actSno);

    @Transactional
    public void updateEndDtm(ResSocketDto resSocketDto);

    @Transactional
    public void updateEnd(ResSocketDto resSocketDto, String reqType);

    @Transactional
    public void updateSdtm(int actSno);
}
