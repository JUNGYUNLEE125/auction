package com.socket.auction.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ReqApiActDto {

	@NotNull
	private int act_sno; // 경매 일련번호

	@NotNull
	private String service;

}
