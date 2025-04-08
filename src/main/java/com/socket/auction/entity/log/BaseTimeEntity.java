package com.socket.auction.entity.log;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass // JPA Entity 클래스들이 BaseTimeEntity를 상속할 경우 필드들(regDate, modDate)도 칼럼으로 인식
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // BaseTimeEntiy 클래스에 Auditing 기능을 포함
public abstract class BaseTimeEntity {
	@CreatedDate // Entity가 생성되어 저장될 때 시간이 자동 저장
	@Column(name = "reg_date", updatable = false, nullable = false)
	private LocalDateTime regDate;

}
