package com.ecommerce.project.exceptions;

import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> response = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(err -> {
            String fieldName = err.getField();
            String message = err.getDefaultMessage();
            response.put(fieldName, message);
        });

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<String> myAPIException(APIException e) {
        String message = e.getMessage();
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoCategoriesFoundException.class)
    public ResponseEntity<String> myNoCategoriesFoundException(NoCategoriesFoundException e) {
        String message = e.getMessage();
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }
}
