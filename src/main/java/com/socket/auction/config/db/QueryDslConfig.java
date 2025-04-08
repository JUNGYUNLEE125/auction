package com.socket.auction.config.db;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
public class QueryDslConfig {

    // 공구마켓 마스터 디비 연결
    @PersistenceContext(unitName = "firstMasterEntityManager")
    private EntityManager firstMasterEntityManager;

    // 공구마켓 슬레이브 디비 연결
    @PersistenceContext(unitName = "firstSlaveEntityManager")
    private EntityManager firstSlaveEntityManager;

    // 공구마켓 로그 디비 연결
    @PersistenceContext(unitName = "firstLogEntityManager")
	private EntityManager firstLogEntityManager;

    // 심쿵할인 마스터 디비 연결
    @PersistenceContext(unitName = "secondMasterEntityManager")
    private EntityManager secondMasterEntityManager;

    // 심쿵할인 슬레이브 디비 연결
    @PersistenceContext(unitName = "secondSlaveEntityManager")
    private EntityManager secondSlaveEntityManager;

    // 심쿵할인 로그 디비 연결
    @PersistenceContext(unitName = "secondLogEntityManager")
    private EntityManager secondLogEntityManager;

    // 할인중독 마스터 디비 연결
    @PersistenceContext(unitName = "thirdMasterEntityManager")
    private EntityManager thirdMasterEntityManager;

    // 할인중독 슬레이브 디비 연결
    @PersistenceContext(unitName = "thirdSlaveEntityManager")
    private EntityManager thirdSlaveEntityManager;

    // 할인중독 로그 디비 연결
    @PersistenceContext(unitName = "thirdLogEntityManager")
    private EntityManager thirdLogEntityManager;

    @Bean
    public JPAQueryFactory firstMasterJpaQueryFactory() {
        return new JPAQueryFactory(firstMasterEntityManager);
    }

    @Bean
    public JPAQueryFactory firstSlaveJpaQueryFactory() {
        return new JPAQueryFactory(firstSlaveEntityManager);
    }

	@Bean
	public JPAQueryFactory firstLogJpaQueryFactory() {
		return new JPAQueryFactory(firstLogEntityManager);
	}

    @Bean
    public JPAQueryFactory secondMasterJpaQueryFactory() {
        return new JPAQueryFactory(secondMasterEntityManager);
    }

    @Bean
    public JPAQueryFactory secondSlaveJpaQueryFactory() {
        return new JPAQueryFactory(secondSlaveEntityManager);
    }

	@Bean
	public JPAQueryFactory secondLogJpaQueryFactory() {
		return new JPAQueryFactory(secondLogEntityManager);
	}

    @Bean
    public JPAQueryFactory thirdMasterJpaQueryFactory() {
        return new JPAQueryFactory(thirdMasterEntityManager);
    }

    @Bean
    public JPAQueryFactory thirdSlaveJpaQueryFactory() {
        return new JPAQueryFactory(thirdSlaveEntityManager);
    }

	@Bean
	public JPAQueryFactory thirdLogJpaQueryFactory() {
		return new JPAQueryFactory(thirdLogEntityManager);
	}

}