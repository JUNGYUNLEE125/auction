package com.socket.auction.repository.third.master;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.ThirdMasterQuerydslRepositorySupport;
import com.socket.auction.entity.UserInfmStatEntity;
import com.socket.auction.entity.QUserInfmStatEntity;

@Repository("third.master.UserInfmStatMstrThrdRepositoryCustomImpl")
public class UserInfmStatMstrThrdRepositoryCustomImpl extends ThirdMasterQuerydslRepositorySupport implements UserInfmStatMstrThrdRepositoryCustom {
    private JPAQueryFactory queryFactory;
    private QUserInfmStatEntity qUserInfmStatEntity;

    public UserInfmStatMstrThrdRepositoryCustomImpl(@Qualifier("thirdMasterJpaQueryFactory") JPAQueryFactory queryFactory) {
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
