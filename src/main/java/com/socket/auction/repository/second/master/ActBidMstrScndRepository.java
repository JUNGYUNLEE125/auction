package com.socket.auction.repository.second.master;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActBidEntity;

@Repository("second.master.ActBidMstrScndRepository")
public interface ActBidMstrScndRepository extends JpaRepository<ActBidEntity, Long>, ActBidMstrScndRepositoryCustom {    
    public int countByActSno(int actSno);

    public ActBidEntity findByActBidSno(int actBidSno);

    public List<ActBidEntity> findByActSnoAndBidAmntGreaterThanEqualOrderByBidAmntDesc(int actSno, int bidAmnt);

    public List<ActBidEntity> findByActSnoOrderByBidAmntDesc(int actSno);

    public List<ActBidEntity> findByActSnoAndBidRsltCdIsNotNull(int actSno);

    public ActBidEntity findTopByActSnoAndMmbrIdOrderByActBidSnoDesc(int actSno, String mmbrId);
}
