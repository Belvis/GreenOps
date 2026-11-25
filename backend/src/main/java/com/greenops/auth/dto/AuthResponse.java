package com.greenops.auth.dto;

public record AuthResponse(String token, String tokenType, String email, String role) {}
