package com.socket.auction.repository.first.slave;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.FirstSlaveQuerydslRepositorySupport;
import com.socket.auction.entity.MmbrEntity;
import com.socket.auction.entity.QMmbrEntity;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("first.slave.MmbrRepositoryCustomImpl")
public class MmbrRepositoryCustomImpl extends FirstSlaveQuerydslRepositorySupport implements MmbrRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QMmbrEntity qMmbrEntity;

    public MmbrRepositoryCustomImpl(@Qualifier("firstSlaveJpaQueryFactory") JPAQueryFactory queryFactory) {
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
