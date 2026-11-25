package com.greenops.gitanalyzer.controller;

import com.greenops.gitanalyzer.dto.GitAnalysisResultDto;
import com.greenops.gitanalyzer.dto.GitAnalyzeRequestDto;
import com.greenops.gitanalyzer.dto.GitHistoryItemDto;
import com.greenops.gitanalyzer.service.GitAnalyzerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/git")
@CrossOrigin(origins = "http://localhost:5173")
public class GitAnalyzerController {

    private final GitAnalyzerService analyzerService;

    public GitAnalyzerController(GitAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<GitAnalysisResultDto> analyzeRepository(@Valid @RequestBody GitAnalyzeRequestDto request) {
        GitAnalysisResultDto result = analyzerService.analyzePublicRepository(request.url());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<GitHistoryItemDto>> getHistory() {
        return ResponseEntity.ok(analyzerService.getAnalysisHistory());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadUrl(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleExternalApiError(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", ex.getMessage()));
    }
}