package com.example.vol.exceptions;

import com.example.vol.events.ReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ApplicationEventPublisher eventPublisher;

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {

        return buildResponse(ex.getStatusCode().value(),
                ex.getReason(),
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidUuid(
            HttpMessageNotReadableException ex, WebRequest request) {

        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife
                && UUID.class.equals(ife.getTargetType())) {

            eventPublisher.publishEvent(
                    new ReservationFailedEvent(
                            this,
                            "INVALID-UUID",
                            null,   // no email
                            0,
                            0,
                            "Invalid UUID format: " + ife.getValue()
                    )
            );

            return buildResponse(HttpStatus.BAD_REQUEST.value(),
                    "Invalid UUID format: " + ife.getValue(),
                    request);
        }

        throw ex;
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            int status, String message, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        body.put("message", message);
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(status).body(body);
    }
}
