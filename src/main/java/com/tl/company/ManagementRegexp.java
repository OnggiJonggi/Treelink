package com.tl.company;

public class ManagementRegexp {
	private ManagementRegexp() {}
	
	// 길이 상수
	public static final int NAME_MAX_LENGTH = 20;
	public static final int MEMO_MAX_LENGTH = 100;
	
	// 정규식
	public static final String NAME = "^[ㄱ-ㅎ가-힣a-zA-Z0-9!@#$%^*()_+\\-=\\[\\]{};:,.?/~₩·\\s]{1," + NAME_MAX_LENGTH + "}$";
	public static final String MEMO = "^[ㄱ-ㅎ가-힣a-zA-Z0-9!@#$%^*()_+\\-=\\[\\]{};:,.?/~₩·\\s]{1," + MEMO_MAX_LENGTH + "}$";
}
