package com.restaurant.restaurant_api.security;

import com.google.firebase.auth.FirebaseToken;
import com.restaurant.restaurant_api.service.FirebaseAuthService;
import com.restaurant.restaurant_api.service.UsuarioService;
import com.restaurant.restaurant_api.exception.TokenInvalidoException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class FirebaseAuthInterceptor implements HandlerInterceptor {
    public static final String ATTR_FIREBASE_TOKEN = "firebaseToken";
    private final FirebaseAuthService firebaseAuthService;
    private final UsuarioService usuarioService;

    public FirebaseAuthInterceptor(FirebaseAuthService firebaseAuthService, UsuarioService usuarioService) {

        this.firebaseAuthService = firebaseAuthService;
        this.usuarioService = usuarioService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String authorizationHeader = request.getHeader("Authorization");
        String idToken = extraerIdToken(authorizationHeader);
        FirebaseToken firebaseToken = firebaseAuthService.verifyToken(idToken);

        request.setAttribute(
                FirebaseAuthInterceptor.ATTR_FIREBASE_TOKEN,
                firebaseToken);

        if (!request.getServletPath().equals("/api/auth/sync")) {

            usuarioService.obtenerUsuarioPorFirebaseUid(
                    firebaseToken.getUid());
        }

        return true;
    }

    private String extraerIdToken(String authorizationHeader) {

        if (authorizationHeader == null) {
            throw new TokenInvalidoException(
                    "No se proporcionó el token de autorización");
        }

        if (!authorizationHeader.regionMatches(
                true, 0, "Bearer ", 0, 7)) {
            throw new TokenInvalidoException(
                    "Formato de token inválido");
        }

        String idToken = authorizationHeader.substring(7);

        if (idToken.isBlank()) {
            throw new TokenInvalidoException(
                    "Token vacío");
        }

        return idToken;
    }
}
