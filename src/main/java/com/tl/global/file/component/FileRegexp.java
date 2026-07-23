package com.tl.global.file.component;

public class FileRegexp {
	private FileRegexp() {};

	// COMPANY_DOC 테이블
	public static final String ORIGINAL_NAME_NO_REGEXP = "^.{1,100}$";
	public static final String DOC_TYPE_REGEXP = "^[^&<>\"';]{1,20}$";
	
	// EVALUATION_DOC 테이블
	public static final String EVAL_MEMO = "^[^&<>\"';]{1,20}$";
}
