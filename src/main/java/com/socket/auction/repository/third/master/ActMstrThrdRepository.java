package com.socket.auction.repository.third.master;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.ActEntity;

@Repository("third.master.ActMstrThrdRepository")
public interface ActMstrThrdRepository extends JpaRepository<ActEntity, Long>, ActMstrThrdRepositoryCustom {
    public ActEntity findByActSno(int actSno);

    public List<ActEntity> findByActStusCd(String actStusCd);

    public List<ActEntity> findByActStusCdAndActEdtmBetween(String actStusCd, String start, String end);

    public List<ActEntity> findByActTypeCdAndActStusCdAndActEdtmBetween(String actTypeCd, String actStusCd, String start, String end);
    
    public int countByActSno(int actSno);
}
