package com.eriksson.rentalsystemhibernate3.exception;

public class InvalidMemberDataException extends RuntimeException {

    public InvalidMemberDataException(String message) {

        super(message);
    }
}