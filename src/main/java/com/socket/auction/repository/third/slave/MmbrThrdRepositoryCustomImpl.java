package com.socket.auction.repository.third.slave;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.ThirdSlaveQuerydslRepositorySupport;
import com.socket.auction.entity.MmbrEntity;
import com.socket.auction.entity.QMmbrEntity;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("third.slave.MmbrRepositoryCustomImpl")
public class MmbrThrdRepositoryCustomImpl extends ThirdSlaveQuerydslRepositorySupport implements MmbrThrdRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QMmbrEntity qMmbrEntity;

    public MmbrThrdRepositoryCustomImpl(@Qualifier("thirdSlaveJpaQueryFactory") JPAQueryFactory queryFactory) {
        super(MmbrEntity.class);
        
        this.queryFactory = queryFactory;
        qMmbrEntity = QMmbrEntity.mmbrEntity;
    }

    @Override
    public MmbrEntity mmbrByMmbrId(String mmbrId) {
        MmbrEntity queryResults = queryFactory.selectFrom(qMmbrEntity)
                                                      .where(qMmbrEntity.mmbrId.eq(mmbrId))
                                                      .fetchOne();
        return queryResults;
    }}
