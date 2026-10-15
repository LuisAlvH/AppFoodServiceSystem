package com.restaurant.restaurant_api.controller;

import com.google.firebase.auth.FirebaseToken;
import com.restaurant.restaurant_api.entity.Usuario;
import com.restaurant.restaurant_api.exception.TokenInvalidoException;
import com.restaurant.restaurant_api.security.FirebaseAuthInterceptor;
import com.restaurant.restaurant_api.service.ResultadoSync;
import com.restaurant.restaurant_api.service.UsuarioService;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import com.restaurant.restaurant_api.dto.response.UserResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final UsuarioService usuarioService;

        public AuthController(UsuarioService usuarioService) {

                this.usuarioService = usuarioService;
        }

        @PostMapping("/sync")
        public ResponseEntity<UserResponse> syncUser(
                        HttpServletRequest request) {

                Object attribute = request.getAttribute(
                                FirebaseAuthInterceptor.ATTR_FIREBASE_TOKEN);

                if (!(attribute instanceof FirebaseToken firebaseToken)) {
                        throw new TokenInvalidoException("Token de Firebase no encontrado");
                }

                String uid = firebaseToken.getUid();
                String email = firebaseToken.getEmail();

                ResultadoSync resultado = usuarioService.obtenerOCrearUsuario(uid, email);
                Usuario usuario = resultado.usuario();

                UserResponse userResponse = new UserResponse(
                                usuario.getIdUsuario(),
                                usuario.getFirebaseUid(),
                                usuario.getEmail());

                HttpStatus status = resultado.creado()
                                ? HttpStatus.CREATED
                                : HttpStatus.OK;
                return ResponseEntity
                                .status(status)
                                .body(userResponse);
        }

        @GetMapping("/me")
        public ResponseEntity<UserResponse> getCurrentUser(
                        HttpServletRequest request) {

                Object attribute = request.getAttribute(
                                FirebaseAuthInterceptor.ATTR_USUARIO_AUTENTICADO);

                if (!(attribute instanceof Usuario usuario)) {
                        throw new TokenInvalidoException(
                                        "Usuario autenticado no encontrado en la petición");
                }

                UserResponse userResponse = new UserResponse(
                                usuario.getIdUsuario(),
                                usuario.getFirebaseUid(),
                                usuario.getEmail());

                return ResponseEntity.ok(userResponse);
        }

}