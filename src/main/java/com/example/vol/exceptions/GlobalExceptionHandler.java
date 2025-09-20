package com.example.vol.exceptions;

import com.example.vol.events.ReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ApplicationEventPublisher eventPublisher;

    // ------------------- ResponseStatusException -------------------
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {

        return buildResponse(ex.getStatusCode().value(),
                ex.getReason(),
                request);
    }

    // ------------------- UUID invalid -------------------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidUuid(
            HttpMessageNotReadableException ex, WebRequest request) {

        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife
                && UUID.class.equals(ife.getTargetType())) {

            // 🔥 Publier un event d’échec dans l’audit
            eventPublisher.publishEvent(
                    new ReservationFailedEvent(
                            this,
                            "INVALID-UUID",
                            null,   // pas d’email
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

    // ------------------- Exceptions métier personnalisées -------------------
    @ExceptionHandler(VolNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleVolNotFound(VolNotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidReservationArgumentException.class,
            PlacesInsuffisantesException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleBadRequestExceptions(Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "BAD_REQUEST");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ------------------- Validation Bean (@Valid) -------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST.value(), errors, request);
    }

    // ------------------- Generic Exception -------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), request);
    }

    // ------------------- Helper -------------------
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
