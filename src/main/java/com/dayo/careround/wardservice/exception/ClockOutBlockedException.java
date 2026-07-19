package com.dayo.careround.wardservice.exception;

public class ClockOutBlockedException extends RuntimeException {
    public ClockOutBlockedException(String message) {
        super(message);
    }
}
