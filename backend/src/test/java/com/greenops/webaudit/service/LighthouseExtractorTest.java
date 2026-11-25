
package com.greenops.webaudit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LighthouseExtractorTest {

    private LighthouseExtractor extractor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        extractor = new LighthouseExtractor();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldExtractFourLighthouseCategoryScores() throws Exception {
        String json = """
                {
                  "lighthouseResult": {
                    "finalDisplayedUrl": "https://example.com",
                    "categories": {
                      "performance": {
                        "title": "Performance",
                        "score": 0.95
                      },
                      "accessibility": {
                        "title": "Accessibility",
                        "score": 0.90
                      },
                      "best-practices": {
                        "title": "Best Practices",
                        "score": 0.85
                      },
                      "seo": {
                        "title": "SEO",
                        "score": 1
                      }
                    },
                    "audits": {}
                  }
                }
                """;

        JsonNode response = objectMapper.readTree(json);

        var result = extractor.extract(response);

        assertEquals("https://example.com", result.analyzedUrl());
        assertEquals(4, result.scores().size());

        assertEquals(95, result.scores().get(0).score());
        assertEquals(90, result.scores().get(1).score());
        assertEquals(85, result.scores().get(2).score());
        assertEquals(100, result.scores().get(3).score());
    }

    @Test
    void shouldExtractFailedAuditAsRecommendation() throws Exception {
        String json = """
                {
                  "lighthouseResult": {
                    "categories": {},
                    "audits": {
                      "uses-optimized-images": {
                        "title": "Properly size images",
                        "description": "Optimize images to improve loading speed.",
                        "score": 0.5,
                        "displayValue": "Potential savings of 200 KiB",
                        "details": {
                          "overallSavingsMs": 300,
                          "overallSavingsBytes": 204800
                        }
                      }
                    }
                  }
                }
                """;

        var result = extractor.extract(objectMapper.readTree(json));

        assertEquals(1, result.recommendations().size());

        var recommendation = result.recommendations().get(0);

        assertEquals("uses-optimized-images", recommendation.id());
        assertEquals("Properly size images", recommendation.title());
        assertEquals(0.5, recommendation.score());
        assertEquals(300.0, recommendation.savingsMs());
        assertEquals(204800.0, recommendation.savingsBytes());
    }

    @Test
    void shouldIgnoreSuccessfulAudits() throws Exception {
        String json = """
                {
                  "lighthouseResult": {
                    "categories": {},
                    "audits": {
                      "valid-audit": {
                        "title": "Successful audit",
                        "description": "Everything is fine.",
                        "score": 1
                      }
                    }
                  }
                }
                """;

        var result = extractor.extract(objectMapper.readTree(json));

        assertTrue(result.recommendations().isEmpty());
    }

    @Test
    void shouldIgnoreAuditsWithoutNumericScore() throws Exception {
        String json = """
                {
                  "lighthouseResult": {
                    "categories": {},
                    "audits": {
                      "not-applicable": {
                        "title": "Audit not applicable",
                        "score": null
                      }
                    }
                  }
                }
                """;

        var result = extractor.extract(objectMapper.readTree(json));

        assertTrue(result.recommendations().isEmpty());
    }

    @Test
    void shouldHandleMissingOptionalSavings() throws Exception {
        String json = """
                {
                  "lighthouseResult": {
                    "categories": {},
                    "audits": {
                      "image-audit": {
                        "title": "Optimize images",
                        "description": "Optimize images.",
                        "score": 0.5
                      }
                    }
                  }
                }
                """;

        var result = extractor.extract(objectMapper.readTree(json));

        assertEquals(1, result.recommendations().size());
        assertNull(result.recommendations().get(0).savingsMs());
        assertNull(result.recommendations().get(0).savingsBytes());
    }

    @Test
    void shouldRejectResponseWithoutLighthouseResult() {
        String json = """
                {
                  "error": {
                    "message": "Invalid URL"
                  }
                }
                """;

        JsonNode response;

        try {
            response = objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> extractor.extract(response)
        );
    }
}
