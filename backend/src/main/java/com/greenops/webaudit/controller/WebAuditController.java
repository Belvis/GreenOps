
package com.greenops.webaudit.controller;

import com.greenops.webaudit.dto.WebAuditResult;
import com.greenops.webaudit.service.JsoupAnalysisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/web-audit")
@CrossOrigin(origins = "http://localhost:5173")
public class WebAuditController {

    private final JsoupAnalysisService jsoupAnalysisService;

    public WebAuditController(JsoupAnalysisService jsoupAnalysisService) {
        this.jsoupAnalysisService = jsoupAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<WebAuditResult> analyze(
            @Valid @RequestBody WebAuditRequest request) {

        WebAuditResult result =
                jsoupAnalysisService.analyze(request.url());

        return ResponseEntity.ok(result);
    }

    public record WebAuditRequest(
            @NotBlank(message = "L'URL est obligatoire")
            @Pattern(
                    regexp = "^https?://.+",
                    message = "L'URL doit commencer par http:// ou https://"
            )
            String url
    ) {
    }
}
