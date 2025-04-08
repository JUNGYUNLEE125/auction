package com.socket.auction.repository.second.slave;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.SecondSlaveQuerydslRepositorySupport;
import com.socket.auction.entity.ActEntity;
import com.socket.auction.entity.PoListEntity;
import com.socket.auction.entity.QActEntity;
import com.socket.auction.entity.QPoListEntity;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("second.slave.PoListScndRepositoryCustomImpl")
public class PoListScndRepositoryCustomImpl extends SecondSlaveQuerydslRepositorySupport implements PoListScndRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QPoListEntity qPoListEntity;
    private QActEntity qActEntity;

    public PoListScndRepositoryCustomImpl(@Qualifier("secondSlaveJpaQueryFactory") JPAQueryFactory queryFactory) {
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
