package com.restaurant.restaurant_api.service;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.restaurant.restaurant_api.exception.FirebaseNoDisponibleException;
import com.restaurant.restaurant_api.exception.TokenInvalidoException;
import com.restaurant.restaurant_api.exception.UsuarioBloqueadoException;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public FirebaseToken verifyToken(String idToken) {

        try {
            return firebaseAuth.verifyIdToken(idToken, true);

        } catch (FirebaseAuthException e) {

            AuthErrorCode authErrorCode = e.getAuthErrorCode();

            // El token no es válido → 401
            if (authErrorCode == AuthErrorCode.INVALID_ID_TOKEN
                    || authErrorCode == AuthErrorCode.EXPIRED_ID_TOKEN
                    || authErrorCode == AuthErrorCode.REVOKED_ID_TOKEN) {

                throw new TokenInvalidoException("Token inválido");
            }

            // El usuario está deshabilitado en Firebase → 403
            if (authErrorCode == AuthErrorCode.USER_DISABLED) {

                throw new UsuarioBloqueadoException(
                        "El usuario está deshabilitado");
            }

            // Firebase no está disponible → 503
            ErrorCode errorCode = e.getErrorCode();

            if (errorCode == ErrorCode.UNAVAILABLE
                    || errorCode == ErrorCode.INTERNAL) {

                throw new FirebaseNoDisponibleException(
                        "Firebase no está disponible",
                        e);
            }

            // Error de autenticación no contemplado → 401
            throw new TokenInvalidoException(
                    "No se pudo validar el token");
        }
    }
}