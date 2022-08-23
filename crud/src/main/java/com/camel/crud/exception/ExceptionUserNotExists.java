package com.camel.crud.exception;

public class ExceptionUserNotExists extends Exception{
    private String message;

    public ExceptionUserNotExists() {
    }

    public ExceptionUserNotExists(String message) {
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
