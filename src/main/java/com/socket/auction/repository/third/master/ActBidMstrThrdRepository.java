package com.socket.auction.repository.third.master;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActBidEntity;

@Repository("third.master.ActBidMstrThrdRepository")
public interface ActBidMstrThrdRepository extends JpaRepository<ActBidEntity, Long>, ActBidMstrThrdRepositoryCustom {    
    public int countByActSno(int actSno);

    public ActBidEntity findByActBidSno(int actBidSno);

    public List<ActBidEntity> findByActSnoAndBidAmntGreaterThanEqualOrderByBidAmntDesc(int actSno, int bidAmnt);

    public List<ActBidEntity> findByActSnoOrderByBidAmntDesc(int actSno);

    public List<ActBidEntity> findByActSnoAndBidRsltCdIsNotNull(int actSno);

    public ActBidEntity findTopByActSnoAndMmbrIdOrderByActBidSnoDesc(int actSno, String mmbrId);
}
