package com.restaurant.restaurant_api.controller;

import com.google.firebase.auth.FirebaseToken;
import com.restaurant.restaurant_api.config.WebConfig;

import com.restaurant.restaurant_api.entity.Usuario;
import com.restaurant.restaurant_api.exception.GlobalExceptionHandler;
import com.restaurant.restaurant_api.security.FirebaseAuthInterceptor;
import com.restaurant.restaurant_api.service.FirebaseAuthService;
import com.restaurant.restaurant_api.service.ResultadoSync;
import com.restaurant.restaurant_api.service.UsuarioService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@Import({
                WebConfig.class,
                FirebaseAuthInterceptor.class,
                GlobalExceptionHandler.class
})
class AuthControllerMvcTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private FirebaseAuthService firebaseAuthService;

        @MockitoBean
        private UsuarioService usuarioService;

        @Test
        void sync_usuarioNuevo_atraviesaInterceptorYDevuelveCreated()
                        throws Exception {

                // Arrange 1: simular la validación del token de Firebase
                FirebaseToken firebaseToken = mock(FirebaseToken.class);

                when(firebaseAuthService.verifyToken("token-valido"))
                                .thenReturn(firebaseToken);

                when(firebaseToken.getUid()).thenReturn("uid-prueba");
                when(firebaseToken.getEmail()).thenReturn("prueba@gmail.com");

                // Arrange 2: simular la creación del usuario por el servicio
                Usuario usuario = new Usuario(
                                1, "uid-prueba", "prueba@gmail.com");

                when(usuarioService.obtenerOCrearUsuario(
                                "uid-prueba", "prueba@gmail.com"))
                                .thenReturn(new ResultadoSync(usuario, true));

                // Act: enviar una petición HTTP simulada a través de Spring MVC
                mockMvc.perform(post("/api/auth/sync")
                                .header("Authorization", "Bearer token-valido")
                                .accept(MediaType.APPLICATION_JSON))

                                // Assert 1: comprobar el código HTTP
                                .andExpect(status().isCreated())

                                // Assert 2: comprobar los datos del JSON
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.firebaseUid").value("uid-prueba"))
                                .andExpect(jsonPath("$.email").value("prueba@gmail.com"));

                // Assert 3: comprobar que Firebase fue consultado para validar el token
                verify(firebaseAuthService).verifyToken("token-valido");

                // Assert 4: comprobar que se sincronizó el usuario
                verify(usuarioService).obtenerOCrearUsuario(
                                "uid-prueba", "prueba@gmail.com");

                // Assert 5: comprobar que el interceptor NO buscó previamente
                // al usuario local por UID antes de llegar al controlador
                verify(usuarioService, never())
                                .obtenerUsuarioPorFirebaseUid(anyString());

                System.out.println("""
                                ==========================================
                                PRUEBA MVC: AuthControllerMvcTest
                                Escenario: sincronización de usuario nuevo
                                Resultado esperado: HTTP 201 Created
                                Verificaciones: respuesta JSON correcta,
                                validación del token y sincronización del usuario.
                                Interceptor: flujo MVC ejecutado según configuración.
                                RESULTADO: PRUEBA SUPERADA
                                ==========================================
                                """);
        }

        @Test
        void me_usuarioAutenticado_atraviesaInterceptorYDevuelveOk()
                        throws Exception {

                // Arrange 1: simular la validación del token de Firebase
                FirebaseToken firebaseToken = mock(FirebaseToken.class);

                when(firebaseAuthService.verifyToken("token-valido"))
                                .thenReturn(firebaseToken);

                when(firebaseToken.getUid())
                                .thenReturn("uid-prueba");

                when(firebaseToken.getEmail())
                                .thenReturn("prueba@gmail.com");

                // Arrange 2: simular que el usuario existe y está activo
                Usuario usuario = new Usuario(
                                1, "uid-prueba", "prueba@gmail.com");

                when(usuarioService.obtenerUsuarioPorFirebaseUid("uid-prueba"))
                                .thenReturn(usuario);

                // Act: enviar una petición HTTP simulada a través de Spring MVC
                mockMvc.perform(get("/api/auth/me")
                                .header("Authorization", "Bearer token-valido")
                                .accept(MediaType.APPLICATION_JSON))

                                // Assert 1: comprobar el código HTTP
                                .andExpect(status().isOk())

                                // Assert 2: comprobar los datos de la respuesta JSON
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.firebaseUid").value("uid-prueba"))
                                .andExpect(jsonPath("$.email").value("prueba@gmail.com"));

                // Assert 3: comprobar que Firebase validó el token
                verify(firebaseAuthService).verifyToken("token-valido");

                verify(usuarioService, times(1))
                                .obtenerUsuarioPorFirebaseUid("uid-prueba");
        }

        @Test
        void me_sinToken_devuelveUnauthorized() throws Exception {

                System.out.println(
                                "TEST: Se solicita /api/auth/me sin autenticación.");

                mockMvc.perform(get("/api/auth/me")
                                .accept(MediaType.APPLICATION_JSON))

                                .andExpect(status().isUnauthorized())

                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("Unauthorized"))
                                .andExpect(jsonPath("$.path").value("/api/auth/me"))
                                .andExpect(jsonPath("$.message").isNotEmpty());

                verifyNoInteractions(firebaseAuthService, usuarioService);

                System.out.println(
                                "RESULTADO: La petición sin token fue rechazada con HTTP 401.");
        }

}