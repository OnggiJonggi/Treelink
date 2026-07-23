package com.tl.company;

public class EvalRegexp {
	private EvalRegexp() {}
	
	// 길이 상수
	public static final int RESON_MAX_LENGTH = 1000;
	public static final int ACTION_RESON_MAX_LENGTH = 100;
	
	// 정규식
	public static final String RESON = "^[ㄱ-ㅎ가-힣a-zA-Z0-9!@#$%^*()_+\\-=\\[\\]{};:,.?/~₩·\\s]{1," + RESON_MAX_LENGTH + "}$";
	public static final String ACTION_RESON = "^[ㄱ-ㅎ가-힣a-zA-Z0-9!@#$%^*()_+\\-=\\[\\]{};:,.?/~₩·\\s]{1," + ACTION_RESON_MAX_LENGTH + "}$";
}
