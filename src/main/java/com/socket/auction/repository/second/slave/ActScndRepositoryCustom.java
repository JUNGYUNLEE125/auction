package com.socket.auction.repository.second.slave;

import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;

@Repository("second.slave.ActScndRepositoryCustom")
public interface ActScndRepositoryCustom {
    public ActEntity getAct(int actSno);

    public void updateEndDtm(ResSocketDto resSocketDto);

    public void updateEnd(ResSocketDto resSocketDto);

    public void updateSdtm(int actSno);
}
