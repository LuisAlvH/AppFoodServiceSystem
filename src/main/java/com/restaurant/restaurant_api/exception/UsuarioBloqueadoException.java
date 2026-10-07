package com.restaurant.restaurant_api.exception;

public class UsuarioBloqueadoException extends RuntimeException {

    public UsuarioBloqueadoException(String message) {
        super(message);
    }
}