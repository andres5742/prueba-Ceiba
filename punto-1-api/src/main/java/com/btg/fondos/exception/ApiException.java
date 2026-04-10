package com.btg.fondos.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ApiException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public static ApiException notFound(String resourceLabel) {
        return new ApiException(HttpStatus.NOT_FOUND, resourceLabel + " no encontrado");
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException insufficientBalance(String fundName) {
        return new ApiException(
                HttpStatus.CONFLICT,
                "No tiene saldo disponible para vincularse al fondo " + fundName);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
