package com.tl.company;

/**
 * MANAGEMENT_STATUS 테이블 STATUS
 */
public enum ManagementStatusEnum {
	READY, // 준비됨
	ONGOING, // 진행 중
	CANCELLED, // 취소됨
	FINISHED, // 완료됨
	;
	
	private ManagementStatusEnum() {};
}
