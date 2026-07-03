package com.tl.member;

public class MemberRegexp {
	// private생성자로 외부(Spring 포함)에서 객체 생성 못 하게 막음
	private MemberRegexp() {}
	
	public static final int ID_MAX_LENGTH = 12;
	public static final int PWD_MAX_LENGTH = 20;
	public static final int NAME_MAX_LENGTH = 10;
	public static final int PHONE_MAX_LENGTH = 13;

	public static final String ID_REGEXP = "^[A-Za-z0-9]{4," + ID_MAX_LENGTH + "}$";
	public static final String PWD_REGEXP = "^[A-Za-z0-9@$!%*#?&]{4," + PWD_MAX_LENGTH  + "}$";
	public static final String NAME_REGEXP = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1," + NAME_MAX_LENGTH + "}$";
	public static final String PHONE_REGEXP = "^(01[016789]|02|0[3-9][0-9])-\\d{3,4}-\\d{4}$";
	
}
