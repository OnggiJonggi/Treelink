package com.tl.global.exception;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	protected void handleCustomException(CustomException e, HttpServletResponse response) throws IOException {
		int status = e.getErrorCode().getHttpStatus().value();
		response.sendError(status, e.getErrorCode().getMessage());
	}

	@ExceptionHandler(ResponseStatusException.class)
	protected void handleResponseStatusException(ResponseStatusException e, HttpServletResponse response)
			throws IOException {
		int status = e.getStatusCode().value();
		String reason = e.getReason() != null ? e.getReason() : "요청을 처리할 수 없습니다";
		response.sendError(status, reason);
	}

	@ExceptionHandler(Exception.class)
	protected void handleException(Exception e, HttpServletResponse response) throws IOException {
		log.error("처리되지 않은 예외 발생", e);
		response.sendError(500, e.getMessage());
	}
}
