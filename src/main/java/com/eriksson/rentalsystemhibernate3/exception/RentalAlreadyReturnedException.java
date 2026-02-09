package com.eriksson.rentalsystemhibernate3.exception;

public class RentalAlreadyReturnedException extends RuntimeException {

    public RentalAlreadyReturnedException(String message) {

        super(message);
    }
}