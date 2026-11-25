
package com.greenops.webaudit.controller;

import com.greenops.webaudit.dto.DomAnalysisResult;
import com.greenops.webaudit.dto.WebAuditResult;
import com.greenops.webaudit.service.JsoupAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebAuditControllerTest {

    @Mock
    private JsoupAnalysisService jsoupAnalysisService;

    @InjectMocks
    private WebAuditController controller;

    @Test
    void shouldReturnAnalysisResult() {
        String url = "https://example.com";

        DomAnalysisResult dom = new DomAnalysisResult(
                url, "Example", 100, 5, 1, 2, 3, 15000
        );

        WebAuditResult expected = new WebAuditResult(
                dom,
                null,
                null,
                "UNAVAILABLE",
                "Test Lighthouse indisponible"
        );

        when(jsoupAnalysisService.analyze(url)).thenReturn(expected);

        var request = new WebAuditController.WebAuditRequest(url);
        var response = controller.analyze(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        assertEquals(dom, response.getBody().dom());
        assertEquals("UNAVAILABLE", response.getBody().lighthouseStatus());

        verify(jsoupAnalysisService).analyze(url);
    }

    @Test
    void shouldPropagateAnalysisException() {
        String url = "https://example.com";

        when(jsoupAnalysisService.analyze(url))
                .thenThrow(new IllegalStateException("Erreur d'analyse"));

        var request = new WebAuditController.WebAuditRequest(url);

        assertThrows(
                IllegalStateException.class,
                () -> controller.analyze(request)
        );

        verify(jsoupAnalysisService).analyze(url);
    }
}
