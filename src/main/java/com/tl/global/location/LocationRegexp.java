package com.tl.global.location;

public class LocationRegexp {
	private LocationRegexp() {}
	
	public static final int ADDRESS_MAX_LENGTH = 100;
	public static final int SIDO_MAX_LENGTH = 20;
	public static final int SIGUNGU_MAX_LENGTH = 20;

	public static final String ADDRESS_REGEXP =  "^[ㄱ-ㅎ가-힣a-zA-Z0-9&.,'\\-·()\\s]{1," + ADDRESS_MAX_LENGTH + "}$";
	public static final String SIDO_REGEXP = "^[ㄱ-ㅎ가-힣a-zA-Z0-9&.,'\\-·()\\s]{1," + SIDO_MAX_LENGTH + "}$";
	public static final String SIGUNGU_REGEXP = "^[ㄱ-ㅎ가-힣a-zA-Z0-9&.,'\\-·()\\s]{0," + SIGUNGU_MAX_LENGTH + "}$";

}
