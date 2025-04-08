package com.socket.auction.repository.first.master;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActBidEntity;

@Repository("first.master.ActBidMstrRepositoryCustom")
public interface ActBidMstrRepositoryCustom {

    public List<ActBidEntity> findTopByActSno(int actSno, int limit);

    public List<ActBidEntity> findTopByActSnoAndBidAmnt(int actSno, int minBid, int limit);
    
    public List<Long> countByActSnoGroupByMmbrId(int actSno);

    public void updateActBid(ActBidEntity actBidEntity);
}
