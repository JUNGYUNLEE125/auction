package com.socket.auction.repository.second.slave;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.SecondSlaveQuerydslRepositorySupport;
import com.socket.auction.entity.MmbrEntity;
import com.socket.auction.entity.QMmbrEntity;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("second.slave.MmbrScndRepositoryCustomImpl")
public class MmbrScndRepositoryCustomImpl extends SecondSlaveQuerydslRepositorySupport implements MmbrScndRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QMmbrEntity qMmbrEntity;

    public MmbrScndRepositoryCustomImpl(@Qualifier("secondSlaveJpaQueryFactory") JPAQueryFactory queryFactory) {
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
