package com.restaurant.restaurant_api.dto.response;

public record UserResponse(
        Integer id,
        String firebaseUid,
        String email) {
}