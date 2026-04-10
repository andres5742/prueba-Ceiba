package com.btg.fondos.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(problemDetail(ex.getHttpStatus(), ex.getMessage()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("Duplicate key: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problemDetail(HttpStatus.CONFLICT, "Ya existe un registro con esos datos únicos"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problemDetail(HttpStatus.CONFLICT, "No se pudo guardar por restricciones de datos"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.debug("Unreadable HTTP body", ex);
        String message = messageFromJsonCause(ex.getMostSpecificCause());
        return ResponseEntity.badRequest().body(problemDetail(HttpStatus.BAD_REQUEST, message));
    }

    private static String messageFromJsonCause(Throwable cause) {
        if (cause instanceof InvalidFormatException invalid) {
            String field =
                    invalid.getPath().stream()
                            .map(JsonMappingException.Reference::getFieldName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("."));
            if (!field.isEmpty()) {
                return "Valor inválido para \"" + field + "\". Revise el tipo y los valores permitidos.";
            }
            return "Formato de valor inválido en el JSON.";
        }
        if (cause instanceof com.fasterxml.jackson.core.JsonParseException) {
            return "JSON mal formado (sintaxis incorrecta).";
        }
        return "JSON inválido o cuerpo incompatible con la solicitud.";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        StringBuilder text = new StringBuilder();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append(fe.getField()).append(": ").append(fe.getDefaultMessage());
        }
        for (ObjectError oe : ex.getBindingResult().getGlobalErrors()) {
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append(oe.getDefaultMessage());
        }
        if (text.isEmpty()) {
            text.append("Datos de entrada no válidos");
        }
        return ResponseEntity.badRequest().body(problemDetail(HttpStatus.BAD_REQUEST, text.toString()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        String message =
                ex.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(" "));
        return ResponseEntity.badRequest()
                .body(problemDetail(HttpStatus.BAD_REQUEST, message.isEmpty() ? "Parámetro no válido" : message));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        String message =
                ex.getAllErrors().stream()
                        .map(
                                e ->
                                        (e instanceof FieldError fe)
                                                ? fe.getField() + ": " + fe.getDefaultMessage()
                                                : e.getDefaultMessage())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "));
        return ResponseEntity.badRequest()
                .body(problemDetail(HttpStatus.BAD_REQUEST, message.isEmpty() ? "Validación fallida" : message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        Object value = ex.getValue();
        String message =
                "Parámetro \""
                        + name
                        + "\" con valor inválido"
                        + (value != null ? ": " + value : "")
                        + ".";
        return ResponseEntity.badRequest().body(problemDetail(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(MissingServletRequestParameterException ex) {
        String message =
                "Falta el parámetro requerido \""
                        + ex.getParameterName()
                        + "\" (tipo "
                        + ex.getParameterType()
                        + ").";
        return ResponseEntity.badRequest().body(problemDetail(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingHeader(MissingRequestHeaderException ex) {
        String message = "Falta la cabecera requerida \"" + ex.getHeaderName() + "\".";
        return ResponseEntity.badRequest().body(problemDetail(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String allowed =
                ex.getSupportedHttpMethods() != null
                        ? ex.getSupportedHttpMethods().stream()
                                .map(m -> m.name())
                                .collect(Collectors.joining(", "))
                        : "";
        String message =
                "Método "
                        + ex.getMethod()
                        + " no permitido para esta ruta"
                        + (allowed.isEmpty() ? "." : ". Permitidos: " + allowed + ".");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(problemDetail(HttpStatus.METHOD_NOT_ALLOWED, message));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        String types =
                ex.getSupportedMediaTypes().stream()
                        .map(MediaType::toString)
                        .collect(Collectors.joining(", "));
        String message =
                "Content-Type no soportado. Use uno de: "
                        + (types.isEmpty() ? "application/json" : types)
                        + ".";
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(problemDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(
                        problemDetail(
                                HttpStatus.NOT_ACCEPTABLE,
                                "No se puede producir una respuesta para los tipos indicados en Accept."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail(HttpStatus.NOT_FOUND, "Recurso no encontrado: " + ex.getResourcePath()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        log.debug("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail(HttpStatus.UNAUTHORIZED, "Autenticación requerida o credenciales inválidas"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        log.debug("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(problemDetail(HttpStatus.FORBIDDEN, "No tiene permiso para acceder a este recurso"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(problemDetail(HttpStatus.PAYLOAD_TOO_LARGE, "El tamaño del archivo supera el máximo permitido"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("Illegal argument: {}", ex.getMessage());
        String message =
                ex.getMessage() != null && !ex.getMessage().isBlank()
                        ? ex.getMessage()
                        : "Solicitud no válida";
        return ResponseEntity.badRequest().body(problemDetail(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        problemDetail(
                                HttpStatus.CONFLICT,
                                ex.getMessage() != null
                                        ? ex.getMessage()
                                        : "Operación no permitida en el estado actual"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor"));
    }

    private static ProblemDetail problemDetail(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
