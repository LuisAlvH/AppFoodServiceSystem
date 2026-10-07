
package com.restaurant.restaurant_api.service;

import com.restaurant.restaurant_api.entity.Usuario;

public record ResultadoSync(
        Usuario usuario,
        boolean creado) {
}