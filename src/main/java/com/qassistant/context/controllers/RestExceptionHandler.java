package com.qassistant.context.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        logger.error("", e);
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) throw e;
        String additionalMessage = "";
        if (e.getCause() != null) {
            additionalMessage = e.getCause().getMessage();
        }
        return new ResponseEntity<>(
                e.getMessage() + "\n" + additionalMessage,
                new HttpHeaders(),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(value = UnsupportedOperationException.class)
    public ResponseEntity<Object> handleUnsupportedOperationException(HttpServletRequest req, UnsupportedOperationException e) {
        logger.error("Unsupported operation exception", e);
        String additionalMessage = "";
        if (e.getCause() != null) {
            additionalMessage = e.getCause().getMessage();
        }
        return new ResponseEntity<>(
                "Unsupported operation exception: " + e.getMessage() + "\n" + additionalMessage,
                new HttpHeaders(),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
