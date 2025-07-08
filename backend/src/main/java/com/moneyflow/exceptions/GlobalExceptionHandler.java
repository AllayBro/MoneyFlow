package com.moneyflow.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex,
                                                                     HttpServletRequest request) {
        logger.warn("Ошибка данных [{}]: {}", request.getRequestURI(), ex.getMessage());
        return jsonError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientError(HttpClientErrorException ex,
                                                                     HttpServletRequest request) {
        logger.warn("HTTP ошибка от клиента [{}]: {} - {}", request.getRequestURI(), ex.getStatusCode(), ex.getResponseBodyAsString());
        return jsonError(ex.getStatusCode(), ex.getResponseBodyAsString());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex,
                                                           HttpServletRequest request) {
        logger.error("Внутренняя ошибка [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return jsonError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера. Повторите позже.");
    }

    private ResponseEntity<Map<String, Object>> jsonError(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message
                ));
    }
}
