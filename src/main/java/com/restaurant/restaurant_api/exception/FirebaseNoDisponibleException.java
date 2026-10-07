package com.restaurant.restaurant_api.exception;

public class FirebaseNoDisponibleException extends RuntimeException {

    public FirebaseNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}