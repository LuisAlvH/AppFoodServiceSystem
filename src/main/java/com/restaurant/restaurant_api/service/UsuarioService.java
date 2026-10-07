package com.restaurant.restaurant_api.service;

import com.restaurant.restaurant_api.entity.EstadoUsuario;
import com.restaurant.restaurant_api.entity.Usuario;
import com.restaurant.restaurant_api.exception.UsuarioBloqueadoException;
import com.restaurant.restaurant_api.exception.UsuarioNoEncontradoException;
import com.restaurant.restaurant_api.repository.UsuarioRepository;
import java.util.Objects;
import java.time.OffsetDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtenerUsuarioPorFirebaseUid(String uid) {

        Usuario usuario = usuarioRepository
                .findByFirebaseUid(uid)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado"));

        validarUsuario(usuario);

        return usuario;
    }

    public ResultadoSync obtenerOCrearUsuario(String uid, String email) {

        return usuarioRepository.findByFirebaseUid(uid)
                .map(usuario -> new ResultadoSync(
                        validarYActualizarAcceso(usuario, email),
                        false))
                .orElseGet(() -> {

                    Usuario usuario = new Usuario(uid, email);
                    usuario.setUltimoAcceso(OffsetDateTime.now());

                    try {

                        Usuario usuarioCreado = usuarioRepository.save(usuario);

                        return new ResultadoSync(
                                usuarioCreado,
                                true);

                    } catch (DataIntegrityViolationException e) {

                        Usuario usuarioExistente = usuarioRepository
                                .findByFirebaseUid(uid)
                                .map(usuarioEncontrado -> validarYActualizarAcceso(
                                        usuarioEncontrado,
                                        email))
                                .orElseThrow(() -> e);

                        return new ResultadoSync(
                                usuarioExistente,
                                false);
                    }
                });
    }

    private Usuario validarYActualizarAcceso(Usuario usuario, String email) {

        validarUsuario(usuario);

        if (email != null && !Objects.equals(usuario.getEmail(), email)) {
            usuario.setEmail(email);

        }

        usuario.setUltimoAcceso(OffsetDateTime.now());

        return usuarioRepository.save(usuario);
    }

    private void validarUsuario(Usuario usuario) {

        if (usuario.getEstado() == EstadoUsuario.BLOQUEADO) {
            throw new UsuarioBloqueadoException(
                    "Usuario bloqueado");
        }
    }
}