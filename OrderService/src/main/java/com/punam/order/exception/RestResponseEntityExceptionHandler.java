package com.punam.order.exception;

import com.punam.order.external.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleProductServiceException(CustomException exception) {
		ErrorResponse errorResponse = ErrorResponse.builder()
										.errorMessage(exception.getMessage())
										.errorCode(exception.getErrorCode())
										.build();
		return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(exception.getStatus()));
	}
}
