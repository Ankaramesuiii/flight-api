package com.example.vol.exceptions;

public class InvalidReservationArgumentException extends RuntimeException {

    public InvalidReservationArgumentException(String message) {
        super(message);
    }
}