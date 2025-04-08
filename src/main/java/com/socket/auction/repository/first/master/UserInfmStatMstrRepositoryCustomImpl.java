package com.socket.auction.repository.first.master;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.FirstMasterQuerydslRepositorySupport;
import com.socket.auction.entity.UserInfmStatEntity;
import com.socket.auction.entity.QUserInfmStatEntity;

@Repository("first.master.UserInfmStatMstrRepositoryCustomImpl")
public class UserInfmStatMstrRepositoryCustomImpl extends FirstMasterQuerydslRepositorySupport implements UserInfmStatMstrRepositoryCustom {
    private JPAQueryFactory queryFactory;
    private QUserInfmStatEntity qUserInfmStatEntity;

    public UserInfmStatMstrRepositoryCustomImpl(@Qualifier("firstMasterJpaQueryFactory") JPAQueryFactory queryFactory) {
        super(UserInfmStatEntity.class);
        
        this.queryFactory = queryFactory;
        qUserInfmStatEntity = QUserInfmStatEntity.userInfmStatEntity;
    }    

    @Override
    @Transactional
    public void updateBadgDspl(UserInfmStatEntity userInfmStatEntity) {

        queryFactory.update(qUserInfmStatEntity)
                    .set(qUserInfmStatEntity.myactBadgDsplYn, userInfmStatEntity.getMyactBadgDsplYn())
                    .where(qUserInfmStatEntity.userSno.eq(userInfmStatEntity.getUserSno()))
                    .execute();
    } 
    
}
