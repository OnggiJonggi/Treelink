package com.tl.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 예외 처리 코드 저장소
 */
@Getter
public enum ErrorCodeEnum {
	CANNOT_LOGIN(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-001", "로그인이 안 되는데요"),
	CANNOT_CREATE_MEMBER(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-002", "새로운 회원을 생성할 수 없습니다."),
	ID_IS_DUPLICATED(HttpStatus.BAD_REQUEST, "MEMBER-003", "이미 사용된 아이디입니다."),
	NICKNAME_IS_DUPLICATED(HttpStatus.BAD_REQUEST, "MEMBER-004", "이미 사용된 닉네임입니다"),
	
	CANNOT_GRANT_ROLE(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-101", "권한을 부여할 수 없습니다"),
	
	BUSINESS_NO_API_NOT_WORKING(HttpStatus.INTERNAL_SERVER_ERROR, "API-101", "API작동 실패"),
	BUSINESS_NO_NULL(HttpStatus.BAD_REQUEST, "API-102", "존재하지 않는 사업자입니다"),
	
	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY-101", "그런 회사 없어요"),
	
	FILE_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE-101", "파일 메타데이터가 없어요"),
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE-102", "그런 파일 없어요"),
	FILE_FORBIDDEN(HttpStatus.BAD_REQUEST, "FILE-103", "잘못된 파일이에요"),
	DOC_TYPE_FORBIDDEN(HttpStatus.BAD_REQUEST, "FILE-104", "서류 타입이 이상해요"),
	FILE_EXPIRE_ON_FORBIDDEN(HttpStatus.BAD_REQUEST, "FILE-105", "파일 만료일이 이상해요");
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
	
	// Enum생성자는 직접 작성이 관례
	private ErrorCodeEnum(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
