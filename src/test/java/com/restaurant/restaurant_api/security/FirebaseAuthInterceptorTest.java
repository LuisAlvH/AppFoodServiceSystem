package com.restaurant.restaurant_api.security;

import com.google.firebase.auth.FirebaseToken;
import com.restaurant.restaurant_api.entity.Usuario;
import com.restaurant.restaurant_api.exception.TokenInvalidoException;
import com.restaurant.restaurant_api.service.FirebaseAuthService;
import com.restaurant.restaurant_api.service.UsuarioService;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthInterceptorTest {

        @Mock
        private FirebaseAuthService firebaseAuthService;

        @Mock
        private UsuarioService usuarioService;

        private FirebaseAuthInterceptor interceptor;

        @BeforeEach
        void setUp() {
                interceptor = new FirebaseAuthInterceptor(firebaseAuthService, usuarioService);
        }

        @Test
        void deberiaLanzarTokenInvalidoCuandoNoExisteHeaderAuthorization()
                        throws Exception {

                System.out.println(
                                "TEST: Se simula una petición sin header Authorization " +
                                                "y se verifica que FirebaseAuthInterceptor lance TokenInvalidoException.");

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();

                assertThrows(
                                TokenInvalidoException.class,
                                () -> interceptor.preHandle(request, response, null));

                System.out.println(
                                "RESULTADO: Se lanzó correctamente TokenInvalidoException "
                                                + "cuando no se proporcionó el header Authorization.");
        }

        @Test
        void deberiaLanzarTokenInvalidoCuandoNoTienePrefijoBearer()
                        throws Exception {

                System.out.println(
                                "TEST: Se simula una petición con Authorization='Basic abc123' " +
                                                "y se verifica que se rechace porque no utiliza el prefijo Bearer.");

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();

                request.addHeader("Authorization", "Basic abc123");

                assertThrows(
                                TokenInvalidoException.class,
                                () -> interceptor.preHandle(request, response, null));

                System.out.println(
                                "RESULTADO: Se lanzó correctamente TokenInvalidoException " +
                                                "porque el header no utiliza el formato Bearer.");
        }

        @Test
        void deberiaLanzarTokenInvalidoCuandoElTokenEstaVacio()
                        throws Exception {

                System.out.println(
                                "TEST: Se simula una petición con Authorization='Bearer ' " +
                                                "y se verifica que se rechace porque el token está vacío.");

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();

                request.addHeader("Authorization", "Bearer ");

                assertThrows(
                                TokenInvalidoException.class,
                                () -> interceptor.preHandle(request, response, null));

                System.out.println(
                                "RESULTADO: Se lanzó correctamente TokenInvalidoException " +
                                                "porque el token está vacío.");
        }

        @Test
        void deberiaValidarTokenYGuardarFirebaseTokenEnRequest()
                        throws Exception {

                System.out.println(
                                "TEST: Se simula una petición con un token Bearer válido " +
                                                "y se verifica que FirebaseAuthService valide el token " +
                                                "y que FirebaseAuthInterceptor lo guarde en el request.");

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();

                String idToken = "token-valido";

                FirebaseToken firebaseToken = mock(FirebaseToken.class);

                when(firebaseAuthService.verifyToken(idToken))
                                .thenReturn(firebaseToken);

                request.setServletPath("/api/auth/sync");
                request.setRequestURI("/api/auth/sync");
                assertEquals("/api/auth/sync", request.getServletPath());
                request.addHeader(
                                "Authorization",
                                "Bearer " + idToken);

                boolean resultado = interceptor.preHandle(
                                request,
                                response,
                                null);

                assertTrue(resultado);

                verify(firebaseAuthService).verifyToken(idToken);

                assertEquals(
                                firebaseToken,
                                request.getAttribute(
                                                FirebaseAuthInterceptor.ATTR_FIREBASE_TOKEN));

                verifyNoInteractions(usuarioService);

                System.out.println(
                                "RESULTADO: El token fue validado correctamente, " +
                                                "guardado en el request y no se consultó UsuarioService " +
                                                "porque el endpoint es /api/auth/sync.");
        }

        @Test
        void deberiaAceptarBearerSinImportarMayusculas()
                        throws Exception {

                System.out.println(
                                "TEST: Se simula una petición con el prefijo 'bearer' " +
                                                "en minúsculas para verificar que la validación sea " +
                                                "case-insensitive.");

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();

                String idToken = "token-valido";

                FirebaseToken firebaseToken = mock(FirebaseToken.class);

                when(firebaseAuthService.verifyToken(idToken))
                                .thenReturn(firebaseToken);

                request.setServletPath("/api/auth/sync");
                request.setRequestURI("/api/auth/sync");

                request.addHeader(
                                "Authorization",
                                "bearer " + idToken);

                boolean resultado = interceptor.preHandle(
                                request,
                                response,
                                null);

                assertTrue(resultado);

                verify(firebaseAuthService).verifyToken(idToken);

                assertEquals(
                                firebaseToken,
                                request.getAttribute(
                                                FirebaseAuthInterceptor.ATTR_FIREBASE_TOKEN));

                System.out.println(
                                "RESULTADO: El prefijo 'bearer' fue aceptado correctamente " +
                                                "sin importar el uso de mayúsculas o minúsculas.");
        }

        @Test
        void deberiaConsultarUsuarioCuandoNoEsEndpointSync()
                        throws Exception {

                System.out.println(
                                "TEST: Se simula una petición autenticada a un endpoint " +
                                                "distinto de /api/auth/sync y se verifica que se consulte " +
                                                "UsuarioService y se guarde el usuario en el request.");

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();

                String idToken = "token-valido";
                String firebaseUid = "firebase-uid-123";

                FirebaseToken firebaseToken = mock(FirebaseToken.class);

                when(firebaseAuthService.verifyToken(idToken))
                                .thenReturn(firebaseToken);

                when(firebaseToken.getUid())
                                .thenReturn(firebaseUid);

                // Preparar el usuario que devolverá el servicio
                Usuario usuario = new Usuario(
                                3, firebaseUid, "actual@gmail.com");

                when(usuarioService.obtenerUsuarioPorFirebaseUid(firebaseUid))
                                .thenReturn(usuario);

                request.setServletPath("/api/auth/me");
                request.setRequestURI("/api/auth/me");

                request.addHeader(
                                "Authorization",
                                "Bearer " + idToken);

                // Ejecutar el interceptor
                boolean resultado = interceptor.preHandle(
                                request,
                                response,
                                null);

                // Comprobar que la petición continúa
                assertTrue(resultado);

                // Verificar que Firebase validó el token
                verify(firebaseAuthService).verifyToken(idToken);

                // Verificar que el servicio consultó al usuario una sola vez
                verify(usuarioService, times(1))
                                .obtenerUsuarioPorFirebaseUid(firebaseUid);

                // Comprobar que el FirebaseToken quedó guardado
                assertEquals(
                                firebaseToken,
                                request.getAttribute(
                                                FirebaseAuthInterceptor.ATTR_FIREBASE_TOKEN));

                // Comprobar que el usuario quedó guardado en la petición
                assertSame(
                                usuario,
                                request.getAttribute(
                                                FirebaseAuthInterceptor.ATTR_USUARIO_AUTENTICADO));

                System.out.println(
                                "RESULTADO: El token fue validado, el usuario fue consultado " +
                                                "una sola vez y quedó guardado en el request.");
        }
}
