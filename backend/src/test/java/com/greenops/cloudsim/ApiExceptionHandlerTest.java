package com.greenops.cloudsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldMapIllegalArgumentExceptionToBadRequest() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleBadRequest(new IllegalArgumentException("vcpu must be positive"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("vcpu must be positive", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldMapIllegalStateExceptionToServiceUnavailable() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleServiceState(new IllegalStateException("No cloud regions configured in database"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("No cloud regions configured in database", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }
}