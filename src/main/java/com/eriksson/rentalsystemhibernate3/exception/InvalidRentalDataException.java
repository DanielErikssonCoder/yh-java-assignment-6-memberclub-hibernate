package com.eriksson.rentalsystemhibernate3.exception;

public class InvalidRentalDataException extends RuntimeException {

    public InvalidRentalDataException(String message) {

        super(message);
    }
}