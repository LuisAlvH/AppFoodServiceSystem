
package com.restaurant.restaurant_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.firebase.auth.FirebaseToken;
import com.restaurant.restaurant_api.dto.response.UserResponse;
import com.restaurant.restaurant_api.entity.Usuario;
import com.restaurant.restaurant_api.exception.TokenInvalidoException;
import com.restaurant.restaurant_api.security.FirebaseAuthInterceptor;
import com.restaurant.restaurant_api.service.ResultadoSync;
import com.restaurant.restaurant_api.service.UsuarioService;

class AuthControllerTest {

        private UsuarioService usuarioService;
        private AuthController controller;

        // Arrange: preparar los objetos comunes antes de cada test
        @BeforeEach
        void setUp() {
                usuarioService = mock(UsuarioService.class);
                controller = new AuthController(usuarioService);
        }

        @Nested
        @DisplayName("POST /api/auth/sync")
        class SyncUserTests {

                @Test
                @DisplayName("Devuelve 201 cuando crea un usuario")
                void syncUser_usuarioNuevo_devuelveCreated() {

                        // 1. Arrange: preparar el token de Firebase
                        FirebaseToken firebaseToken = mock(FirebaseToken.class);

                        when(firebaseToken.getUid()).thenReturn("uid-prueba");
                        when(firebaseToken.getEmail()).thenReturn("prueba@gmail.com");

                        // 2. Arrange: preparar la petición con el token
                        MockHttpServletRequest request = new MockHttpServletRequest();
                        request.setAttribute(
                                        FirebaseAuthInterceptor.ATTR_FIREBASE_TOKEN,
                                        firebaseToken);

                        // 3. Arrange: preparar el usuario que devolverá el servicio
                        Usuario usuario = new Usuario(1, "uid-prueba", "prueba@gmail.com");

                        when(usuarioService.obtenerOCrearUsuario(
                                        "uid-prueba", "prueba@gmail.com"))
                                        .thenReturn(new ResultadoSync(usuario, true));

                        // 4. Act: ejecutar el método del controlador
                        ResponseEntity<UserResponse> respuesta = controller.syncUser(request);

                        // 5. Assert: comprobar el código HTTP y que exista el cuerpo
                        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
                        assertNotNull(respuesta.getBody());

                        // 6. Assert: comprobar los datos devueltos al cliente
                        assertEquals(1, respuesta.getBody().id());
                        assertEquals("uid-prueba", respuesta.getBody().firebaseUid());
                        assertEquals("prueba@gmail.com", respuesta.getBody().email());

                        // 7. Assert: verificar que el controlador llamó al servicio correctamente
                        verify(usuarioService).obtenerOCrearUsuario(
                                        "uid-prueba", "prueba@gmail.com");

                        // 8. Reporte visual: mostrar el resultado en la consola
                        System.out.println("""

                                        ========================================
                                        TEST: AuthControllerTest - syncUser_usuarioNuevo_devuelveCreated
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        Estado HTTP   : %s
                                        ID            : %s
                                        UID           : %s
                                        Email         : %s

                                        ========================================
                                        """.formatted(
                                        respuesta.getStatusCode(),
                                        respuesta.getBody().id(),
                                        respuesta.getBody().firebaseUid(),
                                        respuesta.getBody().email()));
                }

                @Test
                @DisplayName("Devuelve 200 cuando el usuario ya existe")
                void syncUser_usuarioExistente_devuelveOk() {

                        // 1. Arrange: preparar el token de Firebase
                        FirebaseToken firebaseToken = mock(FirebaseToken.class);

                        when(firebaseToken.getUid()).thenReturn("uid-existente");
                        when(firebaseToken.getEmail()).thenReturn("existente@gmail.com");

                        // 2. Arrange: preparar la petición con el token
                        MockHttpServletRequest request = new MockHttpServletRequest();
                        request.setAttribute(
                                        FirebaseAuthInterceptor.ATTR_FIREBASE_TOKEN,
                                        firebaseToken);

                        // 3. Arrange: preparar el usuario existente
                        Usuario usuario = new Usuario(2, "uid-existente", "existente@gmail.com");

                        // Configurar el servicio para indicar que el usuario ya existía
                        when(usuarioService.obtenerOCrearUsuario(
                                        "uid-existente", "existente@gmail.com"))
                                        .thenReturn(new ResultadoSync(usuario, false));

                        // 4. Act: ejecutar el método del controlador
                        ResponseEntity<UserResponse> respuesta = controller.syncUser(request);

                        // 5. Assert: comprobar el código HTTP y que exista el cuerpo
                        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
                        assertNotNull(respuesta.getBody());

                        // 6. Assert: comprobar los datos devueltos al cliente
                        assertEquals(2, respuesta.getBody().id());
                        assertEquals("uid-existente", respuesta.getBody().firebaseUid());
                        assertEquals("existente@gmail.com", respuesta.getBody().email());

                        // 7. Assert: verificar que el controlador llamó al servicio correctamente
                        verify(usuarioService).obtenerOCrearUsuario(
                                        "uid-existente", "existente@gmail.com");

                        // 8. Reporte visual: mostrar el resultado en la consola
                        System.out.println("""

                                        ========================================
                                        TEST: AuthControllerTest - syncUser_usuarioExistente_devuelveOk
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        Estado HTTP   : %s
                                        ID            : %s
                                        UID           : %s
                                        Email         : %s

                                        ========================================
                                        """.formatted(
                                        respuesta.getStatusCode(),
                                        respuesta.getBody().id(),
                                        respuesta.getBody().firebaseUid(),
                                        respuesta.getBody().email()));
                }

                @Test
                @DisplayName("Lanza una excepción cuando falta el token")
                void syncUser_sinTokenFirebase_lanzaTokenInvalidoException() {

                        // 1. Arrange: preparar una petición sin token de Firebase
                        MockHttpServletRequest request = new MockHttpServletRequest();

                        // 2. Act + Assert: ejecutar el controlador y comprobar la excepción esperada
                        TokenInvalidoException excepcion = assertThrows(
                                        TokenInvalidoException.class,
                                        () -> controller.syncUser(request));

                        // 3. Assert: verificar que el servicio no fue llamado
                        verifyNoInteractions(usuarioService);

                        // 4. Reporte visual: mostrar el resultado en la consola
                        System.out.println("""

                                        ========================================
                                        TEST: AuthControllerTest - syncUser_sinTokenFirebase_lanzaTokenInvalidoException
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        Token Firebase : No presente
                                        Excepción      : %s
                                        Servicio       : No fue llamado

                                        ========================================
                                        """.formatted(excepcion.getClass().getSimpleName()));
                }
        }

        @Nested
        @DisplayName("GET /api/auth/me")
        class GetCurrentUserTests {

                @Test
                @DisplayName("Devuelve los datos del usuario autenticado")
                void getCurrentUser_usuarioEncontrado_devuelveOk() {

                        // 1. Arrange: preparar la petición
                        MockHttpServletRequest request = new MockHttpServletRequest();

                        // 2. Arrange: preparar el usuario autenticado que dejó el interceptor
                        Usuario usuario = new Usuario(3, "uid-actual", "actual@gmail.com");

                        request.setAttribute(
                                        FirebaseAuthInterceptor.ATTR_USUARIO_AUTENTICADO,
                                        usuario);

                        // 3. Act: ejecutar el método del controlador
                        ResponseEntity<UserResponse> respuesta = controller.getCurrentUser(request);

                        // 4. Assert: comprobar el código HTTP y que exista el cuerpo
                        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
                        assertNotNull(respuesta.getBody());

                        // 5. Assert: comprobar los datos devueltos al cliente
                        assertEquals(3, respuesta.getBody().id());
                        assertEquals("uid-actual", respuesta.getBody().firebaseUid());
                        assertEquals("actual@gmail.com", respuesta.getBody().email());

                        // 6. Reporte visual: mostrar el resultado en la consola
                        System.out.println("""

                                        ========================================
                                        TEST: AuthControllerTest - getCurrentUser_usuarioEncontrado_devuelveOk
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        Estado HTTP   : %s
                                        ID            : %s
                                        UID           : %s
                                        Email         : %s

                                        ========================================
                                        """.formatted(
                                        respuesta.getStatusCode(),
                                        respuesta.getBody().id(),
                                        respuesta.getBody().firebaseUid(),
                                        respuesta.getBody().email()));
                }
        }
}
