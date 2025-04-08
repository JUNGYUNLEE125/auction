package com.socket.auction.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ReqApiStatusDto {

	@NotNull
	private int act_sno; // 경매 일련번호

	@NotNull
	@Min(1) // 최소값
	@Max(5) // 최대값
	private int act_stus_cd; // 경매상태 (1:경매 시작, 2:입찰 숨기기, 3:입찰 보이기, 4:마감카운팅, 5:종료)

	@NotNull
	private String service;

}
