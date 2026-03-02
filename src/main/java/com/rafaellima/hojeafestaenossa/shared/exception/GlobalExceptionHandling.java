package com.rafaellima.hojeafestaenossa.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandling extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ExceptionCustomized.class)
    public final ResponseEntity<Object> exceptionHandling(ExceptionCustomized ex) {
        int statusCode = Integer.parseInt(ex.getCode());

        return new ResponseEntity<>(
                new ErrorResponse(ex.getCode(), ex.getMessage()),
                HttpStatus.valueOf(statusCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return new ResponseEntity<>(
                new ErrorResponse("500", "Erro interno inesperado"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
