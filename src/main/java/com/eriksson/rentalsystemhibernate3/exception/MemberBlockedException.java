package com.eriksson.rentalsystemhibernate3.exception;

public class MemberBlockedException extends RuntimeException {

    public MemberBlockedException(String message) {

        super(message);
    }
}