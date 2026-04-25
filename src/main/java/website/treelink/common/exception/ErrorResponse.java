package website.treelink.common.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * 예외처리 응답 전용 DTO
 */
@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;
}
