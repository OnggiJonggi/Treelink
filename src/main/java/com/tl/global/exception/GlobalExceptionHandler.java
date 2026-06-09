package com.tl.global.exception;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	protected String handleCustomException(CustomException e, Model model, HttpServletResponse response) {
		
		response.setStatus(e.getErrorCode().getHttpStatus().value());

		model.addAttribute("message", e.getErrorCode().getMessage());
		model.addAttribute("status", e.getErrorCode().getHttpStatus().value());

		return "error/" + e.getErrorCode().getHttpStatus().value();
	}

	// ResponseStatusException 전용 처리도구
	@ExceptionHandler(ResponseStatusException.class)
	protected String handleResponseStatusException(
	        ResponseStatusException e, Model model, HttpServletResponse response) {

	    int status = e.getStatusCode().value();
	    response.setStatus(status);

	    model.addAttribute("error", e.getReason());
	    model.addAttribute("status", status);

	    // 상태코드별 뷰 페이지 분기
	    switch(status) {
	    case 401:
	    case 403:
	    case 404:
	    	return "error/"+status;
	    default : return "error/500";
	    }
	}

	// 일반 예외는 500
	@ExceptionHandler(Exception.class)
	protected String handleException(Exception e, Model model, HttpServletResponse response) {
	    response.setStatus(500);
	    model.addAttribute("error", e.getMessage());
	    return "error/500";
	}
}
