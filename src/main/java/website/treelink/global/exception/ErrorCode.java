package website.treelink.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 예외 처리 코드 저장소
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	CANNOT_LOGIN(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-001", "로그인이 안 되는데요"),
	CANNOT_CREATE_MEMBER(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-002", "새로운 회원을 생성할 수 없습니다."),
	ID_IS_DUPLICATED(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-003", "이미 사용된 아이디입니다."),
	
	CANNOT_GRANT_ROLE(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-101", "권한을 부여할 수 없습니다");
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
