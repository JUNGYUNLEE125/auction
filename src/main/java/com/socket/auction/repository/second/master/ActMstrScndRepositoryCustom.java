package com.socket.auction.repository.second.master;

import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("second.master.ActMstrScndRepositoryCustom")
public interface ActMstrScndRepositoryCustom {
    public ActEntity getAct(int actSno);

    @Transactional
    public void updateEndDtm(ResSocketDto resSocketDto);

    @Transactional
    public void updateEnd(ResSocketDto resSocketDto, String reqType);

    @Transactional
    public void updateSdtm(int actSno);
}
