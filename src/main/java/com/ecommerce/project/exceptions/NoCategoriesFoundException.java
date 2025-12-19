package com.ecommerce.project.exceptions;

public class NoCategoriesFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public NoCategoriesFoundException() {
    }
    public NoCategoriesFoundException(String message) {
        super(message);
    }
}
