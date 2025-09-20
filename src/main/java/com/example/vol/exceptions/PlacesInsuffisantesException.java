package com.example.vol.exceptions;

public class PlacesInsuffisantesException extends RuntimeException {
    public PlacesInsuffisantesException(int demandees, int disponibles) {
        super("Not enough seats available. Requested: " + demandees + ", Available: " + disponibles);
    }
}