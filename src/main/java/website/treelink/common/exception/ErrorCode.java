package website.treelink.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 예외 처리 코드 저장소
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	CANNOT_CREATE_MEMBER(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-001", "새로운 회원을 생성할 수 없습니다."),
	CANNOT_GRANT_ROLE(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER-101", "권한을 부여할 수 없습니다");
	
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
