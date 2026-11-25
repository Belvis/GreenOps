
package com.greenops.webaudit.dto;

public record DomAnalysisResult(
        String url,
        String title,
        int totalElements,
        int totalImages,
        int imagesWithoutAlt,
        int cssFiles,
        int scripts,
        long htmlSizeBytes
) {
}
