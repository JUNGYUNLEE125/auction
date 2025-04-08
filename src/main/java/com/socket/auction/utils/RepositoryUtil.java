package com.socket.auction.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.socket.auction.dto.ReqSocketDto;
import com.socket.auction.dto.ResSocketDto;
import com.socket.auction.dto.UserMmbrInfm;
import com.socket.auction.entity.ActBidEntity;
import com.socket.auction.entity.ActEntity;
import com.socket.auction.entity.ActHistEntity;
import com.socket.auction.entity.ActSetEntity;
import com.socket.auction.entity.GdtlMngmEntity;
import com.socket.auction.entity.MmbrEntity;
import com.socket.auction.entity.MmbrPntEntity;
import com.socket.auction.entity.PoListEntity;
import com.socket.auction.entity.UserEntity;
import com.socket.auction.entity.UserInfmStatEntity;
import com.socket.auction.entity.log.ActApiErrEntity;
import com.socket.auction.repository.first.log.ActApiErrRepository;
import com.socket.auction.repository.first.master.ActBidMstrRepository;
import com.socket.auction.repository.first.master.ActHistMstrRepository;
import com.socket.auction.repository.first.master.ActMstrRepository;
import com.socket.auction.repository.first.master.UserInfmStatMstrRepository;
import com.socket.auction.repository.first.slave.ActBidRepository;
import com.socket.auction.repository.first.slave.ActHistRepository;
import com.socket.auction.repository.first.slave.ActRepository;
import com.socket.auction.repository.first.slave.ActSetRepository;
import com.socket.auction.repository.first.slave.GdtlMngmRepository;
import com.socket.auction.repository.first.slave.MmbrPntRepository;
import com.socket.auction.repository.first.slave.MmbrRepository;
import com.socket.auction.repository.first.slave.PoListRepository;
import com.socket.auction.repository.first.slave.UserInfmStatRepository;
import com.socket.auction.repository.first.slave.UserRepository;
import com.socket.auction.repository.second.log.ActApiErrScndRepository;
import com.socket.auction.repository.second.master.ActBidMstrScndRepository;
import com.socket.auction.repository.second.master.ActHistMstrScndRepository;
import com.socket.auction.repository.second.master.ActMstrScndRepository;
import com.socket.auction.repository.second.master.UserInfmStatMstrScndRepository;
import com.socket.auction.repository.second.slave.ActBidScndRepository;
import com.socket.auction.repository.second.slave.ActHistScndRepository;
import com.socket.auction.repository.second.slave.ActScndRepository;
import com.socket.auction.repository.second.slave.ActSetScndRepository;
import com.socket.auction.repository.second.slave.GdtlMngmScndRepository;
import com.socket.auction.repository.second.slave.MmbrPntScndRepository;
import com.socket.auction.repository.second.slave.MmbrScndRepository;
import com.socket.auction.repository.second.slave.PoListScndRepository;
import com.socket.auction.repository.second.slave.UserInfmStatScndRepository;
import com.socket.auction.repository.second.slave.UserScndRepository;
import com.socket.auction.repository.third.log.ActApiErrThrdRepository;
import com.socket.auction.repository.third.master.ActBidMstrThrdRepository;
import com.socket.auction.repository.third.master.ActHistMstrThrdRepository;
import com.socket.auction.repository.third.master.ActMstrThrdRepository;
import com.socket.auction.repository.third.master.UserInfmStatMstrThrdRepository;
import com.socket.auction.repository.third.slave.ActBidThrdRepository;
import com.socket.auction.repository.third.slave.ActHistThrdRepository;
import com.socket.auction.repository.third.slave.ActSetThrdRepository;
import com.socket.auction.repository.third.slave.ActThrdRepository;
import com.socket.auction.repository.third.slave.GdtlMngmThrdRepository;
import com.socket.auction.repository.third.slave.MmbrPntThrdRepository;
import com.socket.auction.repository.third.slave.MmbrThrdRepository;
import com.socket.auction.repository.third.slave.PoListThrdRepository;
import com.socket.auction.repository.third.slave.UserInfmStatThrdRepository;
import com.socket.auction.repository.third.slave.UserThrdRepository;

/*
 * DB 접속시 3종앱 분기 메소드
 * API 접속시 : HttpServletRequest 도메인 구분으로 3종앱 구분 > ReqSocketDto 저장
 * Socket 접속시 : 요청 파라미터 SERVICE로 구분 > ReqSocketDto 저장
 */

@Component
public class RepositoryUtil {

    @Autowired
    ActRepository actRepository;

    @Autowired
    ActScndRepository actScndRepository;

    @Autowired
    ActThrdRepository actThrdRepository;

    @Autowired
    ActMstrRepository actMstrRepository;

    @Autowired
    ActMstrScndRepository actMstrScndRepository;

    @Autowired
    ActMstrThrdRepository actMstrThrdRepository;

    @Autowired
    ActHistRepository actHistRepository;

    @Autowired
    ActHistScndRepository actHistScndRepository;

    @Autowired
    ActHistThrdRepository actHistThrdRepository;

    @Autowired
    ActHistMstrRepository actHistMstrRepository;

    @Autowired
    ActHistMstrScndRepository actHistMstrScndRepository;

    @Autowired
    ActHistMstrThrdRepository actHistMstrThrdRepository;

    @Autowired
    ActBidRepository actBidRepository;

    @Autowired
    ActBidScndRepository actBidScndRepository;

    @Autowired
    ActBidThrdRepository actBidThrdRepository;

    @Autowired
    ActBidMstrRepository actBidMstrRepository;

    @Autowired
    ActBidMstrScndRepository actBidMstrScndRepository;

    @Autowired
    ActBidMstrThrdRepository actBidMstrThrdRepository;

    @Autowired
    PoListRepository poListRepository;

    @Autowired
    PoListScndRepository poListScndRepository;

    @Autowired
    PoListThrdRepository poListThrdRepository;

    @Autowired
    GdtlMngmRepository gdtlMngmRepository;

    @Autowired
    GdtlMngmScndRepository gdtlMngmScndRepository;

    @Autowired
    GdtlMngmThrdRepository gdtlMngmThrdRepository;

    @Autowired
    MmbrRepository mmbrRepository;

    @Autowired
    MmbrScndRepository mmbrScndRepository;

    @Autowired
    MmbrThrdRepository mmbrThrdRepository;

    @Autowired
    MmbrPntRepository mmbrPntRepository;

    @Autowired
    MmbrPntScndRepository mmbrPntScndRepository;

    @Autowired
    MmbrPntThrdRepository mmbrPntThrdRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserScndRepository userScndRepository;

    @Autowired
    UserThrdRepository userThrdRepository;

	@Autowired
	ActApiErrRepository actApiErrRepository;

	@Autowired
	ActApiErrScndRepository actApiErrScndRepository;

	@Autowired
	ActApiErrThrdRepository actApiErrThrdRepository;

    @Autowired
    UserInfmStatRepository userInfmStatRepository;

    @Autowired
    UserInfmStatScndRepository userInfmStatScndRepository;

    @Autowired
    UserInfmStatThrdRepository userInfmStatThrdRepository;

    @Autowired
    UserInfmStatMstrRepository userInfmStatMstrRepository;

    @Autowired
    UserInfmStatMstrScndRepository userInfmStatMstrScndRepository;

    @Autowired
    UserInfmStatMstrThrdRepository userInfmStatMstrThrdRepository;

    @Autowired
    ActSetRepository actSetRepository;

    @Autowired
    ActSetScndRepository actSetScndRepository;

    @Autowired
    ActSetThrdRepository actSetThrdRepository;

    private Logger logger = LoggerFactory.getLogger(RepositoryUtil.class);

    // act 테이블 actSno(경매번호) 존재 여부 확인
    public int actCountActSno(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();        

        int count = 0;
        switch(service) {
            case "jasonapp019":
                count = actRepository.countByActSno(actSno);
            break;
            case "jasonapp018":
                count = actScndRepository.countByActSno(actSno);
            break;
            case "jasonapp014":
                count = actThrdRepository.countByActSno(actSno);
            break;
        }

        return count;
    }

    // act 테이블 actSno(경매번호) 데이터 추출
    public ActEntity actByActSno(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();   

        ActEntity actEntity = null;
        switch(service) {
            case "jasonapp019":
                actEntity = actRepository.findByActSno(actSno);
            break;
            case "jasonapp018":
                actEntity = actScndRepository.findByActSno(actSno);
            break;
            case "jasonapp014":
                actEntity = actThrdRepository.findByActSno(actSno);
            break;
        }

        return actEntity;
    }

    // act 테이블 경매상태(01:진행예정, 02:진행중, :03종료)로 데이터 추출
    public List<ActEntity> actByActStusCd(ReqSocketDto reqSocketDto, String actStusCd) { 
        String service = reqSocketDto.getService();   
        
        List<ActEntity> actEntityList = null;
        switch(service) {
            case "jasonapp019":
                actEntityList = actRepository.findByActStusCd(actStusCd);
            break;
            case "jasonapp018":
                actEntityList = actScndRepository.findByActStusCd(actStusCd);
            break;
            case "jasonapp014":
                actEntityList = actThrdRepository.findByActStusCd(actStusCd);
            break;
        }

        return actEntityList;
    }

    // act 테이블 경매상태(01:진행예정, 02:진행중, :03종료)로 데이터 추출 마감순 정렬
    public List<ActEntity> actByActStusCdOrderByActEdtm(ReqSocketDto reqSocketDto, String actStusCd) { 
        String service = reqSocketDto.getService();   
        
        List<ActEntity> actEntityList = null;
        switch(service) {
            case "jasonapp019":
                actEntityList = actRepository.findByActStusCdOrderByActEdtm(actStusCd);
            break;
            case "jasonapp018":
                actEntityList = actScndRepository.findByActStusCdOrderByActEdtm(actStusCd);
            break;
            case "jasonapp014":
                actEntityList = actThrdRepository.findByActStusCdOrderByActEdtm(actStusCd);
            break;
        }

        return actEntityList;
    }

    // act 테이블 경매종류(act_type_cd), 경매상태(act_stus_cd), 경매종료일(act_edtm)으로 데이터 추출
    public List<ActEntity> actByActTypeCdAndActStusCdAndActEdtmBetween(ReqSocketDto reqSocketDto, String actTypeCd, String actStusCd, String start, String end) {
        String service = reqSocketDto.getService();   

        List<ActEntity> actEntityList = null;
        switch(service) {
            case "jasonapp019":
                actEntityList = actRepository.findByActTypeCdAndActStusCdAndActEdtmBetween(actTypeCd, actStusCd, start, end);
            break;
            case "jasonapp018":
                actEntityList = actScndRepository.findByActTypeCdAndActStusCdAndActEdtmBetween(actTypeCd, actStusCd, start, end);
            break;
            case "jasonapp014":
                actEntityList = actThrdRepository.findByActTypeCdAndActStusCdAndActEdtmBetween(actTypeCd, actStusCd, start, end);
            break;
        }

        return actEntityList;
    }

    // act 테이블 경매상태(act_stus_cd), 경매종료일(act_edtm)으로 데이터 추출
    public List<ActEntity> actByActStusCdAndActEdtmBetween(ReqSocketDto reqSocketDto, String actStusCd, String start, String end) {
        String service = reqSocketDto.getService();

        List<ActEntity> actEntityList = null;
        switch(service) {
            case "jasonapp019":
                actEntityList = actRepository.findByActStusCdAndActEdtmBetween(actStusCd, start, end);
            break;
            case "jasonapp018":
                actEntityList = actScndRepository.findByActStusCdAndActEdtmBetween(actStusCd, start, end);
            break;
            case "jasonapp014":
                actEntityList = actThrdRepository.findByActStusCdAndActEdtmBetween(actStusCd, start, end);
            break;
        }

        return actEntityList;
    }

    // act 테이블 저장 후, actHistSave 메소드 호출
    public void actSave(ReqSocketDto reqSocketDto, ActEntity actEntity) {
        String service = reqSocketDto.getService();        
        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        actEntity.setFinlEditDtm(nowDate);
        actEntity.setFinlEdtrNs("sockSystm");

        switch(service) {
            case "jasonapp019":
                actMstrRepository.save(actEntity);
            break;
            case "jasonapp018":
                actMstrScndRepository.save(actEntity);
            break;
            case "jasonapp014":
                actMstrThrdRepository.save(actEntity);
            break;
        }

        actHistSave(reqSocketDto, "actSave");
    }

    // act_hist 테이블(경매이력 테이블) 저장
    public void actHistSave(ReqSocketDto reqSocketDto, String methodName) {
        String service = reqSocketDto.getService();
        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        ActEntity actEntity = actByActSno(reqSocketDto);

        ActHistEntity actHistEntity = new ActHistEntity();
        actHistEntity.setChngDtm(nowDate);
        actHistEntity.setActSno(actEntity.getActSno());
        actHistEntity.setPoIdx(actEntity.getPoIdx());
        actHistEntity.setActTypeCd(actEntity.getActTypeCd());
        actHistEntity.setActStusCd(actEntity.getActStusCd());
        actHistEntity.setActRsltCd(actEntity.getActRsltCd());
        actHistEntity.setActSdtm(actEntity.getActSdtm());
        actHistEntity.setActEdtm(actEntity.getActEdtm());
        actHistEntity.setBidUnit(actEntity.getBidUnit());
        actHistEntity.setMaxBidUnit(actEntity.getMaxBidUnit());
        actHistEntity.setBidStrtAmnt(actEntity.getBidStrtAmnt());
        actHistEntity.setBidCurrMaxAmnt(actEntity.getBidCurrMaxAmnt());
        actHistEntity.setBidFinlAmnt(actEntity.getBidFinlAmnt());
        actHistEntity.setBidCnt(actEntity.getBidCnt());
        actHistEntity.setBidrCnt(actEntity.getBidrCnt());
        actHistEntity.setSucsBidrSetCnt(actEntity.getSucsBidrSetCnt());
        actHistEntity.setWaitBidrSetCnt(actEntity.getWaitBidrSetCnt());
        actHistEntity.setAutoExtdUseYn(actEntity.getAutoExtdUseYn());
        actHistEntity.setLiveIntlYn(actEntity.getLiveIntlYn());
        actHistEntity.setSucsBidYn(actEntity.getSucsBidYn());
        actHistEntity.setDelYn(actEntity.getDelYn());
        actHistEntity.setFrstRgsrId(actEntity.getFrstRgsrId());
        actHistEntity.setFrstRegDtm(actEntity.getFrstRegDtm());
        actHistEntity.setFinlEdtrId(actEntity.getFinlEdtrId());
        actHistEntity.setFinlEditDtm(actEntity.getFinlEditDtm());
        actHistEntity.setFinlEdtrNs(actEntity.getFinlEdtrNs());

        logger.info("RepositoryUtil actHistSave "+ methodName +" : "+ actHistEntity);

        switch(service) {
            case "jasonapp019":
                actHistMstrRepository.save(actHistEntity);
            break;
            case "jasonapp018":
                actHistMstrScndRepository.save(actHistEntity);
            break;
            case "jasonapp014":
                actHistMstrThrdRepository.save(actHistEntity);
            break;
        }
    }

    // act 테이블 경매종료시 테이블 업데이트
    public void actUpdateEnd(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto, String reqType) {      
        String service = reqSocketDto.getService();

        switch(service) {
            case "jasonapp019":
                actMstrRepository.updateEnd(resSocketDto, reqType);
            break;
            case "jasonapp018":
                actMstrScndRepository.updateEnd(resSocketDto, reqType);
            break;
            case "jasonapp014":
                actMstrThrdRepository.updateEnd(resSocketDto, reqType);
            break;
        }

        actHistSave(reqSocketDto, "actUpdateEnd");
    }

    // act 테이블 종료일시 업데이트
    public void actUpdateEndDtm(ReqSocketDto reqSocketDto, ResSocketDto resSocketDto) {     
        String service = reqSocketDto.getService();

        switch(service) {
            case "jasonapp019":
                actMstrRepository.updateEndDtm(resSocketDto);
            break;
            case "jasonapp018":
                actMstrScndRepository.updateEndDtm(resSocketDto);
            break;
            case "jasonapp014":
                actMstrThrdRepository.updateEndDtm(resSocketDto);
            break;
        }

        actHistSave(reqSocketDto, "actUpdateEndDtm");
    }

    // act 테이블 시작일시 업데이트
    public void actUpdateSdtm(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();

        switch(service) {
            case "jasonapp019":
                actMstrRepository.updateSdtm(actSno);
            break;
            case "jasonapp018":
                actMstrScndRepository.updateSdtm(actSno);
            break;
            case "jasonapp014":
                actMstrThrdRepository.updateSdtm(actSno);
            break;
        }

        actHistSave(reqSocketDto, "actUpdateSdtm");
    }

    // act_bid 테이블 actSno(경매번호)로 데이터 추출
    public List<ActBidEntity> actBidByActSno(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();

        List<ActBidEntity> actBidList = null;
        switch(service) {
            case "jasonapp019":
                actBidList = actBidRepository.findByActSnoOrderByBidAmntDesc(actSno);
            break;
            case "jasonapp018":
                actBidList = actBidScndRepository.findByActSnoOrderByBidAmntDesc(actSno);
            break;
            case "jasonapp014":
                actBidList = actBidThrdRepository.findByActSnoOrderByBidAmntDesc(actSno);
            break;
        }

        return actBidList;
    }

    // act_bid 테이블 actSno(경매번호)로 데이터 row개수 limit로 추출
    public List<ActBidEntity> actBidByActSnoLimit(ReqSocketDto reqSocketDto, int limit) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();

        List<ActBidEntity> actBidList = null;
        switch(service) {
            case "jasonapp019":
                actBidList = actBidRepository.findTopByActSno(actSno, limit);
            break;
            case "jasonapp018":
                actBidList = actBidScndRepository.findTopByActSno(actSno, limit);
            break;
            case "jasonapp014":
                actBidList = actBidThrdRepository.findTopByActSno(actSno, limit);
            break;
        }

        return actBidList;
    }

    // act_bid 테이블 act_bid_sno(입찰일련번호)로 데이터 추출
    public ActBidEntity actBidByActBidSno(ReqSocketDto reqSocketDto, int actBidSno) {
        String service = reqSocketDto.getService();

        ActBidEntity actBidEntity = null;
        switch(service) {
            case "jasonapp019":
                actBidEntity = actBidRepository.findByActBidSno(actBidSno);
            break;
            case "jasonapp018":
                actBidEntity = actBidScndRepository.findByActBidSno(actBidSno);
            break;
            case "jasonapp014":
                actBidEntity = actBidThrdRepository.findByActBidSno(actBidSno);
            break;
        }

        return actBidEntity;
    }

    // act_bid 테이블 경매번호(actSno), 회원아이디(mmbrId)로 데이터 추출
    public ActBidEntity actBidByActSnoAndMmbrIdOrderByActBidSnoDesc(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();
        String mmbrId  = reqSocketDto.getMmbr_id();

        ActBidEntity actBidEntity = null;
        switch(service) {
            case "jasonapp019":
                actBidEntity = actBidRepository.findTopByActSnoAndMmbrIdOrderByActBidSnoDesc(actSno, mmbrId);
            break;
            case "jasonapp018":
                actBidEntity = actBidScndRepository.findTopByActSnoAndMmbrIdOrderByActBidSnoDesc(actSno, mmbrId);
            break;
            case "jasonapp014":
                actBidEntity = actBidThrdRepository.findTopByActSnoAndMmbrIdOrderByActBidSnoDesc(actSno, mmbrId);
            break;
        }

        return actBidEntity;
    }

    // act_bid 경매번호(actSno)와 row 갯수 limit로 데이터 추출
    public List<ActBidEntity> actBidLimitByActSno(ReqSocketDto reqSocketDto, int limit) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();

        List<ActBidEntity> actBidList = null;
        switch(service) {
            case "jasonapp019":
                actBidList = actBidRepository.findTopByActSno(actSno, limit);
            break;
            case "jasonapp018":
                actBidList = actBidScndRepository.findTopByActSno(actSno, limit);
            break;
            case "jasonapp014":
                actBidList = actBidThrdRepository.findTopByActSno(actSno, limit);
            break;
        }

        return actBidList;
    }

    // act_bid 경매번호(actSno)와 row 갯수 limit로 데이터 추출
    public List<ActBidEntity> actBidMstrLimitByActSno(ReqSocketDto reqSocketDto, int limit) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();

        List<ActBidEntity> actBidList = null;
        switch(service) {
            case "jasonapp019":
                actBidList = actBidMstrRepository.findTopByActSno(actSno, limit);
            break;
            case "jasonapp018":
                actBidList = actBidMstrScndRepository.findTopByActSno(actSno, limit);
            break;
            case "jasonapp014":
                actBidList = actBidMstrThrdRepository.findTopByActSno(actSno, limit);
            break;
        }

        return actBidList;
    }

    // act_bid 테이블 경매번호(actSno), 입찰결과(act_rslt_cd)로 데이터 추출
    public List<ActBidEntity> actBidByActSnoAndBidRsltCdIsNotNull(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();

        List<ActBidEntity> actBidList = null;
        switch(service) {
            case "jasonapp019":
                actBidList = actBidRepository.findByActSnoAndBidRsltCdIsNotNull(actSno);
            break;
            case "jasonapp018":
                actBidList = actBidScndRepository.findByActSnoAndBidRsltCdIsNotNull(actSno);
            break;
            case "jasonapp014":
                actBidList = actBidThrdRepository.findByActSnoAndBidRsltCdIsNotNull(actSno);
            break;
        }

        return actBidList;
    }

    // act_bid 테이블 경매번호(actSno), 입찰금액(bid_amnt)로 데이터 추출
    public List<ActBidEntity> actBidLimitByActSnoAndBidAmnt(ReqSocketDto reqSocketDto, int minBid, int limit) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();

        List<ActBidEntity> actBidList = null;
        switch(service) {
            case "jasonapp019":
                actBidList = actBidRepository.findTopByActSnoAndBidAmnt(actSno, minBid, limit);
            break;
            case "jasonapp018":
                actBidList = actBidScndRepository.findTopByActSnoAndBidAmnt(actSno, minBid, limit);
            break;
            case "jasonapp014":
                actBidList = actBidThrdRepository.findTopByActSnoAndBidAmnt(actSno, minBid, limit);
            break;
        }

        return actBidList;
    }

    // act_bid 테이블 경매번호(actSno), 입찰금액(bid_amnt)금액 이상으로 데이터 추출
    public List<ActBidEntity> actBidActSnoAndBidAmntGreaterThanEqual(ReqSocketDto reqSocketDto, int minBid) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();

        List<ActBidEntity> actBidList = null;
        switch(service) {
            case "jasonapp019":
                // actBidList = actBidRepository.findByActSnoAndBidAmntGreaterThanEqualOrderByBidAmntDesc(actSno, minBid);
                actBidList = actBidMstrRepository.findByActSnoAndBidAmntGreaterThanEqualOrderByBidAmntDesc(actSno, minBid);
            break;
            case "jasonapp018":
                // actBidList = actBidScndRepository.findByActSnoAndBidAmntGreaterThanEqualOrderByBidAmntDesc(actSno, minBid);
                actBidList = actBidMstrScndRepository.findByActSnoAndBidAmntGreaterThanEqualOrderByBidAmntDesc(actSno, minBid);
            break;
            case "jasonapp014":
                // actBidList = actBidThrdRepository.findByActSnoAndBidAmntGreaterThanEqualOrderByBidAmntDesc(actSno, minBid);
                actBidList = actBidMstrThrdRepository.findByActSnoAndBidAmntGreaterThanEqualOrderByBidAmntDesc(actSno, minBid);
            break;
        }

        return actBidList;
    }

    // act_bid 테이블 경매번호로 존재유무 확인
    public int actBidCountByActSno(ReqSocketDto reqSocketDto) {
        int    actSno  = reqSocketDto.getAct_sno();
        String service = reqSocketDto.getService();
        
        int count = 0;
        switch(service) {
            case "jasonapp019":
                count = actBidRepository.countByActSno(actSno);
            break;
            case "jasonapp018":
                count = actBidRepository.countByActSno(actSno);
            break;
            case "jasonapp014":
                count = actBidRepository.countByActSno(actSno);
            break;
        }

        return count;
    }

    // act_bid 테이블 경매번호(actSno), 회원아이디로 그룹핑 > 입찰자수 추출
    public List<Long> actBidCountByActSnoGroupByMmbrId(ReqSocketDto reqSocketDto, int actSno) {
        String service = reqSocketDto.getService();

        List<Long> count = null;
        switch(service) {
            case "jasonapp019":
                count = actBidRepository.countByActSnoGroupByMmbrId(actSno);
            break;
            case "jasonapp018":
                count = actBidScndRepository.countByActSnoGroupByMmbrId(actSno);
            break;
            case "jasonapp014":
                count = actBidThrdRepository.countByActSnoGroupByMmbrId(actSno);
            break;
        }

        return count;
    }

    // act_bid 테이블 update
    public void actBidUpdate(ReqSocketDto reqSocketDto, ActBidEntity actBidEntity) {
        String service = reqSocketDto.getService();

        logger.info("RepositoryUtil actBidUpdate : "+ actBidEntity);

        switch(service) {
            case "jasonapp019":
                actBidMstrRepository.updateActBid(actBidEntity);
            break;
            case "jasonapp018":
                actBidMstrScndRepository.updateActBid(actBidEntity);
            break;
            case "jasonapp014":
                actBidMstrThrdRepository.updateActBid(actBidEntity);
            break;
        }
    }

    // act_bid 테이블 입찰시 저장
    public void actBidSave(ReqSocketDto reqSocketDto, ActBidEntity actBidEntity) {
        String service = reqSocketDto.getService();

        logger.info("RepositoryUtil actBid : "+ actBidEntity);

        switch(service) {
            case "jasonapp019":
                actBidMstrRepository.save(actBidEntity);
            break;
            case "jasonapp018":
                actBidMstrScndRepository.save(actBidEntity);
            break;
            case "jasonapp014":
                actBidMstrThrdRepository.save(actBidEntity);
            break;
        }
    }

    // act_bid 테이블 List로 전달받아 saveAll로 여러개를 한번에 저장
    public void actBidSave(ReqSocketDto reqSocketDto, List<ActBidEntity> actBidEntity) {
        String service = reqSocketDto.getService();

        logger.info("RepositoryUtil actBidList : "+ actBidEntity);

        switch(service) {
            case "jasonapp019":
                actBidMstrRepository.saveAll(actBidEntity);
            break;
            case "jasonapp018":
                actBidMstrScndRepository.saveAll(actBidEntity);
            break;
            case "jasonapp014":
                actBidMstrThrdRepository.saveAll(actBidEntity);
            break;
        }
    }

    // po_list 테이블에서 상품정보 추출
    public PoListEntity poListByPoIdx(ReqSocketDto reqSocketDto, int poIdx) {
        String service = reqSocketDto.getService();
        
        PoListEntity poListEntity = null;
        switch(service) {
            case "jasonapp019":
                poListEntity = poListRepository.findByPoIdx(poIdx);
            break;
            case "jasonapp018":
                poListEntity = poListScndRepository.findByPoIdx(poIdx);
            break;
            case "jasonapp014":
                poListEntity = poListThrdRepository.findByPoIdx(poIdx);
            break;
        }

        return poListEntity;
    }

    // gdtl_mngm 테이블(상품상세)에 상품 상세정보 추출
    public GdtlMngmEntity gdtlMngmByPoIdx(ReqSocketDto reqSocketDto, int poIdx) {
        String service = reqSocketDto.getService();

        GdtlMngmEntity gdtlMngmEntity = null;
        switch(service) {
            case "jasonapp019":
                gdtlMngmEntity = gdtlMngmRepository.findByPoIdx(poIdx);
            break;
            case "jasonapp018":
                gdtlMngmEntity = gdtlMngmScndRepository.findByPoIdx(poIdx);
            break;
            case "jasonapp014":
                gdtlMngmEntity = gdtlMngmThrdRepository.findByPoIdx(poIdx);
            break;
        }

        return gdtlMngmEntity;
    }

    // mmbr(회원) 테이블에서 회원정보 추출
    public MmbrEntity mmbrByMmbrId(ReqSocketDto reqSocketDto, String mmbrId) {   
        String service = reqSocketDto.getService();

        MmbrEntity mmbrEntity = null;
        switch(service) {
            case "jasonapp019":
                mmbrEntity = mmbrRepository.findByMmbrId(mmbrId);
                //mmbrEntity = mmbrRepository.mmbrByMmbrId(mmbrId);
            break;
            case "jasonapp018":
                mmbrEntity = mmbrScndRepository.findByMmbrId(mmbrId);
            break;
            case "jasonapp014":
                mmbrEntity = mmbrThrdRepository.findByMmbrId(mmbrId);
            break;
        }

        return mmbrEntity;
    }

    // mmbr_pnt(회원 패널티) 테이블 패널피 정보 추출
    public MmbrPntEntity mmbrPntByMmbrIdAndPntStusCd(ReqSocketDto reqSocketDto) {  
        String service = reqSocketDto.getService();
        String mmbrId  = reqSocketDto.getMmbr_id();
        
        MmbrPntEntity mmbrPntEntity = null;
        switch(service) {
            case "jasonapp019":
                mmbrPntEntity = mmbrPntRepository.findByMmbrIdAndPntStusCd(mmbrId, "01");  //패널티 진행 상태|CD0209 01: 진행중 02:종료 03: 초기화
            break;
            case "jasonapp018":
                mmbrPntEntity = mmbrPntScndRepository.findByMmbrIdAndPntStusCd(mmbrId, "01");
            break;
            case "jasonapp014":
                mmbrPntEntity = mmbrPntThrdRepository.findByMmbrIdAndPntStusCd(mmbrId, "01");
            break;
        }

        return mmbrPntEntity;
    }

    // user 테이블 회원아이디(mmbrId)로 user_sno 추출
    public UserEntity userByMmbrId(ReqSocketDto reqSocketDto) {       
        String service = reqSocketDto.getService(); 
        String mmbrId  = reqSocketDto.getMmbr_id();
        
        UserEntity userEntity = null;
        switch(service) {
            case "jasonapp019":
                userEntity = userRepository.findByMmbrId(mmbrId);
            break;
            case "jasonapp018":
                userEntity = userScndRepository.findByMmbrId(mmbrId);
            break;
            case "jasonapp014":
                userEntity = userThrdRepository.findByMmbrId(mmbrId);
            break;
        }

        return userEntity;
    }

    // user_infm_stat 테이블에 My > 레드닷 뱃지 정보 추출
    public UserInfmStatEntity userInfmStatByUserSno(ReqSocketDto reqSocketDto, int userSno) {       
        String service = reqSocketDto.getService();
        
        UserInfmStatEntity userInfmStatEntity = null;
        switch(service) {
            case "jasonapp019":
                userInfmStatEntity = userInfmStatRepository.findByUserSno(userSno);
            break;
            case "jasonapp018":
                userInfmStatEntity = userInfmStatScndRepository.findByUserSno(userSno);
            break;
            case "jasonapp014":
                userInfmStatEntity = userInfmStatThrdRepository.findByUserSno(userSno);
            break;
        }

        return userInfmStatEntity;
    }    

    // user_infm_stat 테이블 레드닷 뱃지 업데이트
    public void userInfmStatSave(ReqSocketDto reqSocketDto, UserInfmStatEntity userInfmStatEntity) {
        String service = reqSocketDto.getService();

        logger.info("RepositoryUtil userInfmStatSave : "+ userInfmStatEntity);

        switch(service) {
            case "jasonapp019":
                userInfmStatMstrRepository.save(userInfmStatEntity);
            break;
            case "jasonapp018":
                userInfmStatMstrScndRepository.save(userInfmStatEntity);
            break;
            case "jasonapp014":
                userInfmStatMstrThrdRepository.save(userInfmStatEntity);
            break;
        }
    }  

    // user_infm_stat 테이블 레드닷 뱃지 업데이트
    public UserMmbrInfm getUserMmbrInfm(ReqSocketDto reqSocketDto) {
        String service = reqSocketDto.getService();
        String mmbrId  = reqSocketDto.getMmbr_id();

        UserMmbrInfm userMmbrInfm = null;
        switch(service) {
            case "jasonapp019":
                userMmbrInfm = userRepository.getUserMmbrInfm(mmbrId);
            break;
            case "jasonapp018":
                userMmbrInfm = userScndRepository.getUserMmbrInfm(mmbrId);
            break;
            case "jasonapp014":
                userMmbrInfm = userThrdRepository.getUserMmbrInfm(mmbrId);
            break;
        }

        return userMmbrInfm;
    }  

    // act_set 테이블 마감일 추출
    public ActSetEntity getActSet(ReqSocketDto reqSocketDto) {
        String service = reqSocketDto.getService();

        ActSetEntity actSetEntity = null;
        switch(service) {
            case "jasonapp019":
                actSetEntity = actSetRepository.findByActSetSno(1);
            break;
            case "jasonapp018":
                actSetEntity = actSetScndRepository.findByActSetSno(1);
            break;
            case "jasonapp014":
                actSetEntity = actSetThrdRepository.findByActSetSno(1);
            break;
        }

        return actSetEntity;
    }  

    // api 호출 에러시 로그 저장
    public void saveApiError(ReqSocketDto reqSocketDto, ActApiErrEntity apiErrEntity) {
        String service = reqSocketDto.getService(); 
        
        switch(service) {
            case "jasonapp019":
                actApiErrRepository.save(apiErrEntity);
            break;
            case "jasonapp018":
                actApiErrScndRepository.save(apiErrEntity);
            break;
            case "jasonapp014":
                actApiErrThrdRepository.save(apiErrEntity);
            break;
        }
    } 

    // 통합상품관리 수정 
    public ActEntity getTorderPoIdx019(String tpoIdx) {
        ActEntity actEntity = poListRepository.getTorderPoIdx(tpoIdx);

        return actEntity;
    }
    
    public ActEntity getTorderPoIdx018(String tpoIdx) {
        ActEntity actScndEntity = poListScndRepository.getTorderPoIdx(tpoIdx);

        return actScndEntity;
    }
    
    public ActEntity getTorderPoIdx014(String tpoIdx) {
        ActEntity actThrdEntity = poListThrdRepository.getTorderPoIdx(tpoIdx);

        return actThrdEntity;
    }
}
