package com.socket.auction.config.db;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

// 공구마켓 로그 디비 연결

@Repository
public abstract class FirstLogQuerydslRepositorySupport extends QuerydslRepositorySupport {

	/**
	 * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
	 *
	 * @param domainClass must not be {@literal null}.
	 */
	public FirstLogQuerydslRepositorySupport(Class<?> domainClass) {
		super(domainClass);
	}

	@Override
	@PersistenceContext(unitName = "firstLogEntityManager")
	public void setEntityManager(EntityManager entityManager) {
		super.setEntityManager(entityManager);
	}
}