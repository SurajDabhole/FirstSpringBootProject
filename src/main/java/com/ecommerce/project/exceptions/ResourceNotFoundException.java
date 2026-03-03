package com.ecommerce.project.exceptions;

public class ResourceNotFoundException extends RuntimeException{
    String resourceName;
    String field;
    String fieldName;
    Long fieldId;

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String message){
        super(message);
    }
    public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
        super(String.format("%s not found with %s : %d", resourceName, field, fieldId));
        this.field = field;
        this.fieldId = fieldId;
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException(String field, Long fieldId, String resourceName) {
        super(String.format("%s not found with %s : %d", resourceName, field, fieldId));
        this.resourceName = resourceName;
        this.fieldId = fieldId;
        this.field = field;
    }
}
