package com.eriksson.rentalsystemhibernate3.exception;

public class MemberAlreadyExistsException extends RuntimeException {

    public MemberAlreadyExistsException(String message) {

        super(message);
    }
}