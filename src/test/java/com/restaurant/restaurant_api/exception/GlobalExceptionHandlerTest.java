package com.restaurant.restaurant_api.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;// Simulamos peticiones HTTP para probar el GlobalExceptionHandler sin necesidad
                            // de levantar un servidor completo.

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void deberiaRetornar403CuandoUsuarioEstaBloqueado() throws Exception {

        System.out.println(
                "TEST: Se simula una petición HTTP a un endpoint que lanza " +
                        "UsuarioBloqueadoException y se verifica que GlobalExceptionHandler " +
                        "responda con HTTP 403 y el ErrorResponse esperado.");

        mockMvc.perform(
                get("/test/bloqueado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Usuario bloqueado"))
                .andExpect(jsonPath("$.path").value("/test/bloqueado"));
    }

    @Test
    void deberiaRetornar401CuandoTokenEsInvalido() throws Exception {

        System.out.println(
                "TEST: Se simula una petición HTTP a un endpoint que lanza " +
                        "TokenInvalidoException y se verifica que GlobalExceptionHandler " +
                        "responda con HTTP 401 y el ErrorResponse esperado.");

        mockMvc.perform(
                get("/test/token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token inválido"))
                .andExpect(jsonPath("$.path").value("/test/token"));
    }

    @Test
    void deberiaRetornar404CuandoUsuarioNoExiste() throws Exception {

        System.out.println(
                "TEST: Se simula una petición HTTP a un endpoint que lanza " +
                        "UsuarioNoEncontradoException y se verifica que GlobalExceptionHandler " +
                        "responda con HTTP 404 y el ErrorResponse esperado.");

        mockMvc.perform(
                get("/test/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"))
                .andExpect(jsonPath("$.path").value("/test/no-encontrado"));
    }

    @Test
    void deberiaRetornar503CuandoFirebaseNoEstaDisponible() throws Exception {

        System.out.println(
                "TEST: Se simula una petición HTTP a un endpoint que lanza " +
                        "FirebaseNoDisponibleException y se verifica que GlobalExceptionHandler " +
                        "responda con HTTP 503 y el ErrorResponse esperado.");

        mockMvc.perform(
                get("/test/firebase"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("Firebase no disponible"))
                .andExpect(jsonPath("$.path").value("/test/firebase"));
    }

    @Test
    void deberiaRetornar409CuandoHayConflictoDeIntegridad() throws Exception {

        System.out.println(
                "TEST: Se simula una petición HTTP a un endpoint que lanza " +
                        "DataIntegrityViolationException y se verifica que GlobalExceptionHandler " +
                        "responda con HTTP 409 sin exponer el mensaje interno de la excepción.");

        mockMvc.perform(
                get("/test/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("La operación entra en conflicto con los datos existentes"))
                .andExpect(jsonPath("$.path").value("/test/conflicto"));
    }

    @Test
    void deberiaRetornar500CuandoOcurreUnaExcepcionNoControlada() throws Exception {

        System.out.println(
                "TEST: Se simula una excepción no contemplada específicamente " +
                        "por GlobalExceptionHandler y se verifica que el catch-all " +
                        "responda con HTTP 500 sin exponer información interna.");

        mockMvc.perform(
                get("/test/error-interno"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocurrió un error interno"))
                .andExpect(jsonPath("$.path").value("/test/error-interno"));
    }

    @RestController
    static class DummyController {

        @GetMapping("/test/token")
        public void tokenInvalido() {
            throw new TokenInvalidoException("Token inválido");
        }

        @GetMapping("/test/no-encontrado")
        public void usuarioNoEncontrado() {
            throw new UsuarioNoEncontradoException("Usuario no encontrado");
        }

        @GetMapping("/test/bloqueado")
        public void usuarioBloqueado() {
            throw new UsuarioBloqueadoException("Usuario bloqueado");
        }

        @GetMapping("/test/firebase")
        public void firebaseNoDisponible() {
            throw new FirebaseNoDisponibleException("Firebase no disponible", null);
        }

        @GetMapping("/test/conflicto")
        public void conflicto() {
            throw new DataIntegrityViolationException("UID duplicado");
        }

        @GetMapping("/test/error-interno")
        public void errorInterno() {
            throw new RuntimeException("Error interno de prueba");
        }
    }

}
