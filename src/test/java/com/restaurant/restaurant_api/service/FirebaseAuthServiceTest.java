package com.restaurant.restaurant_api.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.AuthErrorCode;
import com.restaurant.restaurant_api.exception.TokenInvalidoException;
import com.restaurant.restaurant_api.exception.UsuarioBloqueadoException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import com.google.firebase.auth.FirebaseAuthException;

@ExtendWith(MockitoExtension.class)
public class FirebaseAuthServiceTest {

    @Mock
    FirebaseAuth firebaseAuth;

    @InjectMocks
    FirebaseAuthService firebaseAuthService;

    @Test
    void deberiaDevolverFirebaseTokenCuandoElTokenEsValido()
            throws FirebaseAuthException {

        System.out.println(
                "TEST: Se mockea FirebaseAuth para simular una validación exitosa del token " +
                        "y se verifica que FirebaseAuthService devuelva el mismo FirebaseToken.");

        String idToken = "token-valido";

        FirebaseToken firebaseToken = mock(FirebaseToken.class);

        when(firebaseAuth.verifyIdToken(idToken, true))
                .thenReturn(firebaseToken);

        FirebaseToken resultado = firebaseAuthService.verifyToken(idToken);

        assertSame(firebaseToken, resultado);
    }

    @Test
    void deberiaLanzarTokenInvalidoCuandoElTokenEsInvalido()
            throws FirebaseAuthException {

        System.out.println(
                "TEST: Se mockea FirebaseAuth para simular un token inválido " +
                        "y se verifica que FirebaseAuthService lance TokenInvalidoException.");

        String idToken = "token-invalido";

        FirebaseAuthException excepcion = new FirebaseAuthException(
                ErrorCode.INVALID_ARGUMENT,
                "Token inválido",
                null,
                null,
                AuthErrorCode.INVALID_ID_TOKEN);

        when(firebaseAuth.verifyIdToken(idToken, true))
                .thenThrow(excepcion);

        assertThrows(
                TokenInvalidoException.class,
                () -> firebaseAuthService.verifyToken(idToken));
    }

    @Test
    void deberiaLanzarTokenInvalidoCuandoElTokenEstaExpirado()
            throws FirebaseAuthException {

        System.out.println(
                "TEST: Se mockea FirebaseAuth para simular un token expirado " +
                        "y se verifica que FirebaseAuthService lance TokenInvalidoException.");

        String idToken = "token-expirado";

        FirebaseAuthException excepcion = new FirebaseAuthException(
                ErrorCode.INVALID_ARGUMENT,
                "Token expirado",
                null,
                null,
                AuthErrorCode.EXPIRED_ID_TOKEN);

        when(firebaseAuth.verifyIdToken(idToken, true))
                .thenThrow(excepcion);

        assertThrows(
                TokenInvalidoException.class,
                () -> firebaseAuthService.verifyToken(idToken));
    }

    @Test
    void deberiaLanzarTokenInvalidoCuandoElTokenEstaRevocado()
            throws FirebaseAuthException {

        System.out.println(
                "TEST: Se mockea FirebaseAuth para simular un token revocado " +
                        "y se verifica que FirebaseAuthService lance TokenInvalidoException.");

        String idToken = "token-revocado";

        FirebaseAuthException excepcion = new FirebaseAuthException(
                ErrorCode.INVALID_ARGUMENT,
                "Token revocado",
                null,
                null,
                AuthErrorCode.REVOKED_ID_TOKEN);

        when(firebaseAuth.verifyIdToken(idToken, true))
                .thenThrow(excepcion);

        assertThrows(
                TokenInvalidoException.class,
                () -> firebaseAuthService.verifyToken(idToken));
    }

    @Test
    void deberiaLanzarUsuarioBloqueadoCuandoElUsuarioEstaDeshabilitado()
            throws FirebaseAuthException {

        System.out.println(
                "TEST: Se mockea FirebaseAuth para simular un usuario deshabilitado " +
                        "y se verifica que FirebaseAuthService lance UsuarioBloqueadoException.");

        String idToken = "token-usuario-bloqueado";

        FirebaseAuthException excepcion = new FirebaseAuthException(
                ErrorCode.INVALID_ARGUMENT,
                "Usuario deshabilitado",
                null,
                null,
                AuthErrorCode.USER_DISABLED);

        when(firebaseAuth.verifyIdToken(idToken, true))
                .thenThrow(excepcion);

        assertThrows(
                UsuarioBloqueadoException.class,
                () -> firebaseAuthService.verifyToken(idToken));
    }
}