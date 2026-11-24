
package com.greenops.webaudit.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class PageSpeedServiceTest {

    private RestClient.Builder builder;
    private MockRestServiceServer mockServer;
    private PageSpeedService service;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();

        mockServer = MockRestServiceServer
                .bindTo(builder)
                .build();

        service = new PageSpeedService(builder, "test-api-key");
    }

    @Test
    void shouldReturnGooglePageSpeedResponse() {

        String jsonResponse = """
                {
                    "lighthouseResult": {
                        "categories": {
                            "performance": {
                                "score": 0.95
                            }
                        }
                    }
                }
                """;

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString(
                                        "www.googleapis.com/pagespeedonline/v5/runPagespeed"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "url=https"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "key=test-api-key"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "strategy=mobile"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "category=PERFORMANCE"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "category=ACCESSIBILITY"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "category=BEST_PRACTICES"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "category=SEO"
                                )
                        )
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        jsonResponse,
                        MediaType.APPLICATION_JSON
                ));

        JsonNode result = service.analyze("https://example.com");

        assertNotNull(result);

        assertEquals(
                0.95,
                result.path("lighthouseResult")
                        .path("categories")
                        .path("performance")
                        .path("score")
                        .asDouble()
        );

        mockServer.verify();
    }

    @Test
    void shouldRejectMissingApiKey() {

        PageSpeedService serviceWithoutKey =
                new PageSpeedService(
                        RestClient.builder(),
                        ""
                );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> serviceWithoutKey.analyze(
                        "https://example.com"
                )
        );

        assertEquals(
                "La clé API Google PageSpeed est manquante.",
                exception.getMessage()
        );
    }

    @Test
    void shouldHandleGoogleApiError() {

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "www.googleapis.com/pagespeedonline/v5/runPagespeed"
                        )
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.TOO_MANY_REQUESTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("""
                                        {
                                            "error": {
                                                "code": 429,
                                                "message": "Quota exceeded"
                                            }
                                        }
                                        """)
                );

        assertThrows(
                RestClientResponseException.class,
                () -> service.analyze("https://example.com")
        );

        mockServer.verify();
    }
}
