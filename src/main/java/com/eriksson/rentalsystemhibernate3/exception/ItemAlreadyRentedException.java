package com.eriksson.rentalsystemhibernate3.exception;

public class ItemAlreadyRentedException extends RuntimeException {

    public ItemAlreadyRentedException(String message) {

        super(message);
    }
}