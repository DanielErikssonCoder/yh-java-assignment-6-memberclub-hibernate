package com.eriksson.rentalsystemhibernate3.exception;

public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {

        super(message);
    }
}