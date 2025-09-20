package com.example.vol.exceptions;

public class VolNotFoundException extends RuntimeException {
    public VolNotFoundException(String volId) {
        super("Flight not found with ID: " + volId);
    }
}