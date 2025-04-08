package com.socket.auction.repository.third.master;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.ThirdMasterQuerydslRepositorySupport;
import com.socket.auction.entity.ActBidEntity;
import com.socket.auction.entity.QActBidEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("third.master.ActBidMstrThrdRepositoryCustomImpl")
public class ActBidMstrThrdRepositoryCustomImpl extends ThirdMasterQuerydslRepositorySupport implements ActBidMstrThrdRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QActBidEntity qActBidEntity;

    public ActBidMstrThrdRepositoryCustomImpl(@Qualifier("thirdMasterJpaQueryFactory") JPAQueryFactory queryFactory) {
        super(ActBidEntity.class);
        
        this.queryFactory = queryFactory;
        qActBidEntity = QActBidEntity.actBidEntity;
    }

    @Override
    public List<ActBidEntity> findTopByActSno(int actSno, int limit) {
        List<ActBidEntity> queryResults = queryFactory.selectFrom(qActBidEntity)
                                                      .where(qActBidEntity.actSno.eq(actSno))
                                                      .orderBy(qActBidEntity.actBidSno.desc())
                                                      .limit(limit)
                                                      .fetch();
        return queryResults;
    }

    @Override
    public List<ActBidEntity> findTopByActSnoAndBidAmnt(int actSno, int minBid, int limit) {
        List<ActBidEntity> queryResults = queryFactory.selectFrom(qActBidEntity)
                                                      .where(qActBidEntity.actSno.eq(actSno))
                                                      .where(qActBidEntity.bidAmnt.goe(minBid))
                                                      .orderBy(qActBidEntity.actBidSno.desc())
                                                      .limit(limit)
                                                      .fetch();
        return queryResults;
    }

    @Override
    public List<Long> countByActSnoGroupByMmbrId(int actSno) {   
        List<Long> queryResults = queryFactory.select(qActBidEntity.mmbrId.count())
                                        .from(qActBidEntity)
                                        .groupBy(qActBidEntity.mmbrId)
                                        .where(qActBidEntity.actSno.eq(actSno))
                                        .fetch();
        return queryResults;
    }        

    @Override
    @Transactional
    public void updateActBid(ActBidEntity actBidEntity) {

        queryFactory.update(qActBidEntity)
                    .set(qActBidEntity.bidRsltCd, actBidEntity.getBidRsltCd())
                    .set(qActBidEntity.waitBidrSeq, actBidEntity.getWaitBidrSeq())
                    .set(qActBidEntity.sucsBidDtm, actBidEntity.getBidDtm())
                    .set(qActBidEntity.payPsblDtm, actBidEntity.getPayPsblDtm())
                    .set(qActBidEntity.finlEditDtm, actBidEntity.getFinlEditDtm())
                    .set(qActBidEntity.finlEdtrNs, actBidEntity.getFinlEdtrNs() )
                    .where(qActBidEntity.actBidSno.eq(actBidEntity.getActBidSno()))
                    .execute();
    }    
}
