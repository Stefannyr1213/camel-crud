package com.camel.crud.exception;

public class ExceptionUserNotFound extends Exception{
    private String message;

    public ExceptionUserNotFound() {
    }

    public ExceptionUserNotFound(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
