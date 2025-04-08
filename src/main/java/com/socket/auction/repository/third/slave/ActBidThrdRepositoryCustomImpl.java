package com.socket.auction.repository.third.slave;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.ThirdSlaveQuerydslRepositorySupport;
import com.socket.auction.entity.ActBidEntity;
import com.socket.auction.entity.QActBidEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("third.slave.ActBidThrdRepositoryCustomImpl")
public class ActBidThrdRepositoryCustomImpl extends ThirdSlaveQuerydslRepositorySupport implements ActBidThrdRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QActBidEntity qActBidEntity;

    public ActBidThrdRepositoryCustomImpl(@Qualifier("thirdSlaveJpaQueryFactory") JPAQueryFactory queryFactory) {
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
}
