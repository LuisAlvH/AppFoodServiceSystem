package com.restaurant.restaurant_api.repository;

import com.restaurant.restaurant_api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByFirebaseUid(String firebaseUid);
}