package com.jirapat.personalfinance.api.exception;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException (String message) {
        super(message);
    }
}
