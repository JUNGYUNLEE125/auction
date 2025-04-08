package com.socket.auction.repository.first.slave;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.socket.auction.config.db.FirstSlaveQuerydslRepositorySupport;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.entity.ActEntity;
import com.socket.auction.entity.QActEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("first.slave.ActRepositoryCustomImpl")
public class ActRepositoryCustomImpl extends FirstSlaveQuerydslRepositorySupport implements ActRepositoryCustom  {
    private JPAQueryFactory queryFactory;
    private QActEntity qActEntity;

    public ActRepositoryCustomImpl(@Qualifier("firstSlaveJpaQueryFactory") JPAQueryFactory queryFactory) {
        super(ActEntity.class);
        
        this.queryFactory = queryFactory;
        qActEntity = QActEntity.actEntity;
    }

    @Override
    public ActEntity getAct(int actSno) {   
        ActEntity queryResults = queryFactory.selectFrom(qActEntity) 
                                             .where(qActEntity.actSno.eq(actSno))
                                             .fetchOne();
        return queryResults;
    }   

    @Override
    @Transactional
    public void updateEnd(ResSocketDto resSocketDto) {   
        // String sucsBidYn = "N";
        String actRsltCd = resSocketDto.getData().getAct_info().getAct_rslt_cd();
        String nowDate   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // if("01".equals(actRsltCd)) sucsBidYn = "Y";

        int bidFinlAmnt = (int) Math.round(resSocketDto.getData().getAct_stus().getMax_bid());

        if("01".equals(resSocketDto.getData().getAct_info().getAct_type_cd())){
            queryFactory.update(qActEntity)
                    .set(qActEntity.actStusCd, "03")
                    .set(qActEntity.actRsltCd, actRsltCd)
                    .set(qActEntity.bidCurrMaxAmnt, bidFinlAmnt)
                    .set(qActEntity.bidFinlAmnt, bidFinlAmnt)
                    // .set(qActEntity.sucsBidYn, sucsBidYn)
                    .set(qActEntity.finlEditDtm, nowDate)
                    .set(qActEntity.finlEdtrNs, "sockSystm")
                    .where(qActEntity.actSno.eq(resSocketDto.getData().getAct_info().getAct_sno()))
                    .execute();
        } else {
            queryFactory.update(qActEntity)
                    .set(qActEntity.actStusCd, "03")
                    .set(qActEntity.actRsltCd, actRsltCd)
                    .set(qActEntity.actEdtm, nowDate)
                    .set(qActEntity.bidCurrMaxAmnt, bidFinlAmnt)
                    .set(qActEntity.bidFinlAmnt, bidFinlAmnt)
                    // .set(qActEntity.sucsBidYn, sucsBidYn)
                    .set(qActEntity.finlEditDtm, nowDate)
                    .set(qActEntity.finlEdtrNs, "sockSystm")
                    .where(qActEntity.actSno.eq(resSocketDto.getData().getAct_info().getAct_sno()))
                    .execute();
        }        
    }     

    @Override
    @Transactional
    public void updateEndDtm(ResSocketDto resSocketDto) {

        queryFactory.update(qActEntity)
                    .set(qActEntity.actEdtm, resSocketDto.getData().getAct_info().getAct_edtm())
                    .set(qActEntity.finlEditDtm, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .set(qActEntity.finlEdtrNs, "sockSystm")
                    .where(qActEntity.actSno.eq(resSocketDto.getData().getAct_info().getAct_sno()))
                    .execute();
    }     

    @Override
    @Transactional
    public void updateSdtm(int actSno) {   
        String nowDate   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        queryFactory.update(qActEntity)
                .set(qActEntity.actSdtm, nowDate)
                .set(qActEntity.finlEditDtm, nowDate)
                .set(qActEntity.finlEdtrNs, "sockSystm")
                .where(qActEntity.actSno.eq(actSno))
                .execute();        
    } 
}
