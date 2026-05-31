package com.tl.global.file;

public class FileRegexp {
	private FileRegexp() {};

	public static final String ORIGINAL_NAME_NO_REGEXP = "^.{1,100}$";
	public static final String DOC_TYPE_REGEXP = "^[^&<>\"';]{1,20}$";
}
