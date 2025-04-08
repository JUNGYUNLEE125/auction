package com.socket.auction.repository.first.slave;

import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("first.slave.ActRepositoryCustom")
public interface ActRepositoryCustom {
    public ActEntity getAct(int actSno);

    @Transactional
    public void updateEndDtm(ResSocketDto resSocketDto);

    @Transactional
    public void updateEnd(ResSocketDto resSocketDto);

    @Transactional
    public void updateSdtm(int actSno);
}
