package com.socket.auction.repository.second.slave;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.SecondSlaveQuerydslRepositorySupport;
import com.socket.auction.dto.UserMmbrInfm;
import com.socket.auction.entity.QMmbrEntity;
import com.socket.auction.entity.QUserEntity;
import com.socket.auction.entity.QUserInfmStatEntity;
import com.socket.auction.entity.UserEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("second.slave.UserScndRepositoryCustomImpl")
public class UserScndRepositoryCustomImpl extends SecondSlaveQuerydslRepositorySupport implements UserScndRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QUserEntity qUserEntity;
    private QMmbrEntity qMmbrEntity;
    private QUserInfmStatEntity qUserInfmStatEntity;

    public UserScndRepositoryCustomImpl(@Qualifier("secondSlaveJpaQueryFactory") JPAQueryFactory queryFactory) {
        super(UserEntity.class);
        
        this.queryFactory = queryFactory;
        qUserEntity = QUserEntity.userEntity;
        qMmbrEntity = QMmbrEntity.mmbrEntity;
        qUserInfmStatEntity = QUserInfmStatEntity.userInfmStatEntity;
    }

    @Override
    public UserMmbrInfm getUserMmbrInfm(String mmbrId) {   
        List<Tuple> queryResults = queryFactory.select(qUserEntity.userSno, qUserEntity.mmbrId, qMmbrEntity.actPntYn, qUserInfmStatEntity.myactBadgDsplYn) 
                                             .from(qUserEntity)
                                             .leftJoin(qMmbrEntity).on(qMmbrEntity.mmbrId.eq(qUserEntity.mmbrId))
                                             .leftJoin(qUserInfmStatEntity).on(qUserInfmStatEntity.userSno.eq(qUserEntity.userSno))
                                             .where(qUserEntity.mmbrId.eq(mmbrId))
                                             .fetch();
                                             
        UserMmbrInfm userMmbrInfm = new UserMmbrInfm();
        if(queryResults.size() > 0) {
            userMmbrInfm.setUserSno(queryResults.get(0).get(0, int.class));
            userMmbrInfm.setMmbrId(queryResults.get(0).get(1, String.class));
            userMmbrInfm.setActPntYn(queryResults.get(0).get(2, String.class));
            userMmbrInfm.setMyactBadgDsplYn(queryResults.get(0).get(3, String.class));
        }

        return userMmbrInfm;
    }   
}
