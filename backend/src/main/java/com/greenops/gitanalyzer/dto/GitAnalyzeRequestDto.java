package com.greenops.gitanalyzer.dto;

import jakarta.validation.constraints.NotBlank;

public record GitAnalyzeRequestDto(
        @NotBlank(message = "L'URL publique du dépôt Git est obligatoire.")
        String url
) {}