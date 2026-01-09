package com.financial.analytics.bonds.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.financial.analytics.bonds.dto.ErrorResponse;

@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
        String traceId = UUID.randomUUID().toString();
        ErrorResponse er = new ErrorResponse("ERR_VALIDATION", "Validation failed", errors, HttpStatus.BAD_REQUEST.value(), traceId);
        log.info("Validation failed (traceId={}) fields={}", traceId, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(er);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleBadJson(HttpMessageNotReadableException ex) {
        String traceId = UUID.randomUUID().toString();
        // Log the exception details with trace id for server-side debugging
        log.warn("Malformed JSON request (traceId={})", traceId, ex);
        ErrorResponse er = new ErrorResponse("ERR_INVALID_PAYLOAD", "Malformed or invalid JSON payload", null, HttpStatus.BAD_REQUEST.value(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(er);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unexpected error (traceId={})", traceId, ex);
        ErrorResponse er = new ErrorResponse("ERR_INTERNAL", "An unexpected error occurred", null, HttpStatus.INTERNAL_SERVER_ERROR.value(), traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(er);
    }
}
