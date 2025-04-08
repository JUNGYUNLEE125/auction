package com.socket.auction.repository.third.slave;

import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.entity.ActEntity;

import org.springframework.stereotype.Repository;

@Repository("third.slave.ActThrdRepositoryCustom")
public interface ActThrdRepositoryCustom {
    public ActEntity getAct(int actSno);

    public void updateEndDtm(ResSocketDto resSocketDto);

    public void updateEnd(ResSocketDto resSocketDto);

    public void updateSdtm(int actSno);
}
