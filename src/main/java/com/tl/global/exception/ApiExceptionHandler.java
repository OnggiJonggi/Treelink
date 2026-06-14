package com.tl.global.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

	@ExceptionHandler(CustomException.class)
	protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		ErrorCodeEnum errorCode = e.getErrorCode();

		ErrorResponse response = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(errorCode.getHttpStatus().value())
				.error(errorCode.getHttpStatus().name())
				.code(errorCode.getCode())
				.message(errorCode.getMessage())
				.build();

		return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
	}
	
	@ExceptionHandler(ResponseStatusException.class)
	protected ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
	    HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());

	    ErrorResponse response = ErrorResponse.builder()
	            .timestamp(LocalDateTime.now())
	            .status(status.value())
	            .error(status.name())
	            .code(status.name())
	            .message(e.getReason() != null ? e.getReason() : status.getReasonPhrase())
	            .build();

	    return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception e) {
		ErrorResponse response = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.code("INTERNAL_SERVER_ERROR")
				.message(e.getMessage())
				.build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
