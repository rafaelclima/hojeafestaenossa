package com.rafaellima.hojeafestaenossa.shared.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    public String code;
    public String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
