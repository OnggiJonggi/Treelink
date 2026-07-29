package com.tl.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 예외 처리 코드 저장소
 * 
 * code
 * 000번대 : 일반
 * 100번대 : 권한 / 특수
 * 200번대 : API / 기타
 */
@Getter
public enum ErrorCodeEnum {
	
	/**
	 * MEMBER
	 */
	CANNOT_LOGIN(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-001", "로그인이 안 되는데요"),
	CANNOT_CREATE_MEMBER(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-002", "새로운 회원을 생성할 수 없습니다"),
	ID_IS_DUPLICATED(HttpStatus.BAD_REQUEST, "MEMBER-003", "이미 사용된 아이디입니다"),
	NICKNAME_IS_DUPLICATED(HttpStatus.BAD_REQUEST, "MEMBER-004", "이미 사용된 닉네임입니다"),
	FAILED_CREATE_ACCOUNT(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-005", "신규 계정 생성에 실패했습니다"),
	FAILED_UPDATE_MEMBER(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-006", "회원 정보 수정에 실패했습니다"),
	
	CANNOT_UPDATE_MY_STATUS(HttpStatus.FORBIDDEN, "MEMBER-101", "자신의 상태는 수정할 수 없습니다"),
	
	
	/**
	 * ROLE
	 */
	FAILED_GRANT_ROLE(HttpStatus.INTERNAL_SERVER_ERROR, "ROLE-001", "권한 부여에 실패했습니다"),
	FAILED_DELETE_ROLE(HttpStatus.INTERNAL_SERVER_ERROR, "ROLE-002", "권한을 삭제에 실패했습니다"),
	FAILED_CREATE_CONSULTANT_LEADER(HttpStatus.INTERNAL_SERVER_ERROR, "ROLE-003", "대표 컨설턴트 권한 추가에 실패했습니다"),
	
	CANNOT_CREATE_SUPER_ADMIN(HttpStatus.FORBIDDEN, "ROLE-101", "최고 관리자 생성 권한이 없습니다"),
	CANNOT_CREATE_ADMIN(HttpStatus.FORBIDDEN, "ROLE-102", "관리자 생성 권한이 없습니다."),
	CANNOT_CHANGE_MY_ROLE(HttpStatus.FORBIDDEN, "ROLE-103", "자신의 권한은 수정할 수 없습니다"),
	CANNOT_DELETE_ADMIN_ACCOUNT(HttpStatus.FORBIDDEN, "ROLE-104", "관리자 계정은 삭제할 수 없습니다."),

	/**
	 * COMPANY
	 */
	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY-001", "해당 업체를 찾을 수 없습니다"),
	FAILED_CREATE_COMPANY(HttpStatus.INTERNAL_SERVER_ERROR, "COMPANY-002", "업체 생성에 실패했습니다"),
	FAILED_CREATE_COMPANY_SPECIALTY(HttpStatus.INTERNAL_SERVER_ERROR, "COMPANY-003", "업체 주 종목 생성에 실패했습니다"),
	FAILED_UPDATE_COMPANY(HttpStatus.INTERNAL_SERVER_ERROR, "COMPANY-004", "업체 생성에 실패했습니다"),
	FAILED_DELETE_UNUSED_INTRO_IMAGAE(HttpStatus.INTERNAL_SERVER_ERROR, "COMPANY-005", "사용하지 않은 업체 소개문 사진을 DB에서 지울 수 없습니다"),
	FAILED_CREATE_COMPANY_LOCATION(HttpStatus.INTERNAL_SERVER_ERROR, "COMPANY-006", "업체-장소 생성에 실패했습니다"),
	FAILED_DELETE_COMPANY_LOCATION(HttpStatus.INTERNAL_SERVER_ERROR, "COMPANY-007", "업체-장소 삭제에 실패했습니다"),
	FAILED_CREATE_COMPANY_MANAGEMNET(HttpStatus.INTERNAL_SERVER_ERROR, "COMPANY-008", "업체 관리 현황 생성에 실패했습니다"),
	
	NO_PERMISSION_FOR_VIEW_COMPANY(HttpStatus.FORBIDDEN, "COMPANY-101", "업체 열람 권한이 없습니다"),
	
	BUSINESS_NO_API_NOT_WORKING(HttpStatus.INTERNAL_SERVER_ERROR, "COMPANY-201", "국세청 사업자등록정보 진위확인 API가 작동하지 않습니다"),
	BUSINESS_NO_NULL(HttpStatus.BAD_REQUEST, "COMPANY-202", "존재하지 않는 사업자입니다"),
	
	/**
	 * EVALUATION
	 */
	INVALIED_EVAL_FORMAT(HttpStatus.BAD_REQUEST, "EVAL-001", "평가 형식이 잘못되었습니다"),
	FAILED_CREATE_EVAL(HttpStatus.INTERNAL_SERVER_ERROR, "EVAL-002", "평가 생성에 실패했습니다"),
	FAILED_CREATE_EVAL_SCORE(HttpStatus.INTERNAL_SERVER_ERROR, "EVAL-003", "평가 점수 생성에 실패했습니다"),
	FAILED_UPDATE_EVAL(HttpStatus.INTERNAL_SERVER_ERROR, "EVAL-004", "평가 수정에 실패했습니다"),
	FAILED_UPDATE_EVAL_SCORE(HttpStatus.INTERNAL_SERVER_ERROR, "EVAL-005", "평가 점수 수정에 실패했습니다"),
	FAILED_CREATE_EVAL_HISTORY(HttpStatus.INTERNAL_SERVER_ERROR, "EVAL-006", "평가 기록 생성에 실패했습니다"),
	FAILED_CREATE_EVAL_SCORE_HISTORY(HttpStatus.INTERNAL_SERVER_ERROR, "EVAL-007", "평가 점수 기록 생성에 실패했습니다"),
	FAILED_CREATE_EVAL_DOC(HttpStatus.INTERNAL_SERVER_ERROR, "EVAL-008", "평가 서류 생성에 실패했습니다"),
	FAILED_UPDATE_EVAL_DOC(HttpStatus.INTERNAL_SERVER_ERROR, "EVAL-009", "평가 서류 수정에 실패했습니다"),
	
	
	/**
	 * LOCATION
	 */
	FAILED_CREATE_LOCATION(HttpStatus.INTERNAL_SERVER_ERROR, "LOCATION-001", "위치 추가에 실패했습니다"),
	FAILED_DELETE_LOCATION(HttpStatus.INTERNAL_SERVER_ERROR, "LOCATION-002", "위치 삭제에 실패했습니다"),
	
	GEOCODING_API_NOT_WORKING(HttpStatus.INTERNAL_SERVER_ERROR, "LOCATION-201", "지오코딩 API가 작동하지 않습니다"),
	GEOCODING_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION-202", "지오코딩 결과가 없습니다"),
	
	/**
	 * FILE
	 */
	FAILED_CREATE_FILE_HISTORY(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-002", "파일 기록 생성에 실패했습니다"),
	FILE_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE-003", "파일 메타데이터가 없어요"),
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE-004", "그런 파일 없어요"),
	FILE_FORBIDDEN(HttpStatus.BAD_REQUEST, "FILE-005", "잘못된 파일이에요"),
	DOC_TYPE_FORBIDDEN(HttpStatus.BAD_REQUEST, "FILE-006", "서류 타입이 이상해요"),
	FILE_EXPIRE_ON_FORBIDDEN(HttpStatus.BAD_REQUEST, "FILE-007", "파일 만료일이 이상해요"),
	
	
	/**
	 * 에러페이지 실험용
	 */
	DUMMY_ERROR_CODE_4XX(HttpStatus.TOO_MANY_REQUESTS, "DUMMY-4XX", "실험용 오류 코드 - 4XX"),
	DUMMY_ERROR_CODE_400(HttpStatus.BAD_REQUEST, "DUMMY-400", "실험용 오류 코드 - 400"),
	DUMMY_ERROR_CODE_401(HttpStatus.UNAUTHORIZED, "DUMMY-401", "실험용 오류 코드 - 401"),
	DUMMY_ERROR_CODE_403(HttpStatus.FORBIDDEN, "DUMMY-403", "실험용 오류 코드 - 403"),
	DUMMY_ERROR_CODE_404(HttpStatus.NOT_FOUND, "DUMMY-404", "실험용 오류 코드 - 404"),
	DUMMY_ERROR_CODE_405(HttpStatus.METHOD_NOT_ALLOWED, "DUMMY-405", "실험용 오류 코드 - 405"),
	DUMMY_ERROR_CODE_5XX(HttpStatus.BAD_GATEWAY, "DUMMY-5XX", "실험용 오류 코드 - 5XX"),
	DUMMY_ERROR_CODE_500(HttpStatus.INTERNAL_SERVER_ERROR, "DUMMY-500", "실험용 오류 코드 - 500"),
	DUMMY_ERROR_CODE_ERROR(HttpStatus.OK, "DUMMY-ERROR", "실험용 오류 코드 - ERROR"),
	
	
	;
	
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
