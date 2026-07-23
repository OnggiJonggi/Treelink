package com.tl.company;

/**
 * MANAGEMENT_STATUS 테이블 VISIBLE
 * 누구에게 얼만큼 보여줄건가요?
 */
public enum ManagementVisibleEnum {
	ALL, // 제한 없음
	SEMI, // 숨기되 전체 관리 수에는 포함해요
	HIDDEN, // 전부 숨겨요
	;
	
	private ManagementVisibleEnum() {};
}
