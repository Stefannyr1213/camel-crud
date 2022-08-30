package com.camel.crud.exception;

public class ExceptionUserRepeat extends Exception {
    private String message;

    public ExceptionUserRepeat() {
    }

    public ExceptionUserRepeat(String message) {
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
