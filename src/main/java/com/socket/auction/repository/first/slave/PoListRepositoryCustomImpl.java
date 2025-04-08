package com.socket.auction.repository.first.slave;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.FirstSlaveQuerydslRepositorySupport;
import com.socket.auction.entity.ActEntity;
import com.socket.auction.entity.PoListEntity;
import com.socket.auction.entity.QActEntity;
import com.socket.auction.entity.QPoListEntity;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("first.slave.PoListRepositoryCustomImpl")
public class PoListRepositoryCustomImpl extends FirstSlaveQuerydslRepositorySupport implements PoListRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QPoListEntity qPoListEntity;
    private QActEntity qActEntity;

    public PoListRepositoryCustomImpl(@Qualifier("firstSlaveJpaQueryFactory") JPAQueryFactory queryFactory) {
        super(PoListEntity.class);
        
        this.queryFactory = queryFactory;
        qPoListEntity = QPoListEntity.poListEntity;
        qActEntity = QActEntity.actEntity;
    }

    @Override
    public ActEntity getTorderPoIdx(String tpoIdx) {   
        ActEntity queryResults = queryFactory.select(qActEntity) 
                                             .from(qPoListEntity)
                                             .leftJoin(qActEntity).on(qActEntity.poIdx.eq(qPoListEntity.poIdx))
                                             .where(qPoListEntity.torderPoIdx.eq(tpoIdx))
                                             .fetchOne();

        return queryResults;
    }   
}
