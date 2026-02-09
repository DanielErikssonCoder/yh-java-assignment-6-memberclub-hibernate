package com.eriksson.rentalsystemhibernate3.exception;

public class MemberHasActiveRentalsException extends RuntimeException {

    public MemberHasActiveRentalsException(String message) {

        super(message);
    }
}