package com.example.mygymbro.exceptions;

public class DAOException extends GymBroException {
    public DAOException(String message, Throwable cause) {
        super(message);
        this.initCause(cause); // Manteniamo la causa originale (es. SQL error) per il debug
    }
}