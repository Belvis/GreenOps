package com.greenops.cloudsim;

public record ApiErrorResponse(
        String timestamp,
        int status,
        String error,
        String message
) {
}