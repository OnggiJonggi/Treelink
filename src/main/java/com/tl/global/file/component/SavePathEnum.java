package com.tl.global.file.component;

import lombok.Getter;

// 파일 저장 경로 저장
@Getter
public enum SavePathEnum {
	COMPANY_DOC("company-doc/"),
	COMPANY_LOGO("image/company-logo/"),
	COMPANY_INTRO("image/company-intro/"),
	;
	
	private final String folder;

	
	// 이넘 생성자는 직접 작성이 관례
	private SavePathEnum(String folder) {
		this.folder = folder;
	}
}
