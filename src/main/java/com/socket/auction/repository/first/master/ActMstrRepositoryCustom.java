package com.socket.auction.repository.first.master;

import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;

@Repository("first.master.ActMstrRepositoryCustom")
public interface ActMstrRepositoryCustom {
    public ActEntity getAct(int actSno);

    public void updateEndDtm(ResSocketDto resSocketDto);

    public void updateEnd(ResSocketDto resSocketDto, String reqType);

    public void updateSdtm(int actSno);
}
