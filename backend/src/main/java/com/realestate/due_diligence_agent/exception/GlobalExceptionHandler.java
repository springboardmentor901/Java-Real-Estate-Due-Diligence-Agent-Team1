package com.realestate.due_diligence_agent.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ExternalApiException.class)
public ResponseEntity<Map<String, Object>> handleExternalApiException(
        ExternalApiException exception) {

    Map<String, Object> response = new HashMap<>();

    response.put("status", HttpStatus.BAD_GATEWAY.value());
    response.put("error", "Bad Gateway");
    response.put("message", exception.getMessage());

    return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(response);
}

    // Property not found → 404 Not Found
    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyNotFound(
            PropertyNotFoundException exception) {

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    // Other runtime errors → 400 Bad Request
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException exception) {

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}