package com.financial.analytics.bonds.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        String errorCode, // machine-friendly error code (e.g. ERR_VALIDATION)
        String message, // human-friendly message
        Map<String, String> errors, // field -> message for validation
        int httpStatus, // numeric HTTP status
        String traceId,
        Instant timestamp
) {
    public ErrorResponse(String errorCode, String message, Map<String, String> errors, int httpStatus, String traceId) {
        this(errorCode, message, errors, httpStatus, traceId, Instant.now());
    }
}
