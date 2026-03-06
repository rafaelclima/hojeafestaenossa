package com.rafaellima.hojeafestaenossa.shared.exception;

public class UnauthorizedException extends ExceptionCustomized {

    public UnauthorizedException(String code, String message) {
        super(code, message);
    }

}