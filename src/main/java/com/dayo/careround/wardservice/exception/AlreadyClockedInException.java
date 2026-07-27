package com.dayo.careround.wardservice.exception;

public class AlreadyClockedInException extends RuntimeException {
    public AlreadyClockedInException(String message) {
        super(message);
    }
}
