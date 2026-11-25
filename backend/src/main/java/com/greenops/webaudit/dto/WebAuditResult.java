
package com.greenops.webaudit.dto;

import com.greenops.webaudit.service.LighthouseExtractor;

public record WebAuditResult(
        DomAnalysisResult dom,
        LighthouseExtractor.LighthouseAnalysis lighthouse,
        EcoIndexResult ecoIndex,
        String lighthouseStatus,
        String lighthouseError
) {

    public record EcoIndexResult(
            double score,
            char grade,
            double co2Grams,
            double waterCl
    ) {}
}
