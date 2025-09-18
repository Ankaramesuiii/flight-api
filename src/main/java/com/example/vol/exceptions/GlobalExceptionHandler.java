package com.example.vol.exceptions;

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
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        System.out.println("Handling ResponseStatusException: " + ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getStatusCode());
        body.put("message", ex.getReason()); // your custom message
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidUuid(HttpMessageNotReadableException ex, WebRequest request) {
        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormatEx) {
            // Check if the target type is UUID
            if (UUID.class.equals(invalidFormatEx.getTargetType())) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", 404);
                body.put("error", "NOT_FOUND");
                body.put("message", "Flight not found");
                body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());
                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
            }
        }

        // rethrow if it's a different parsing error
        throw ex;
    }


}
