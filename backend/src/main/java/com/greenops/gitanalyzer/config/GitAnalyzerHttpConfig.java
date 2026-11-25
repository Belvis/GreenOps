package com.greenops.gitanalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GitAnalyzerHttpConfig {

    @Bean
    public RestClient gitAnalyzerRestClient(RestClient.Builder builder) {
        return builder
                .defaultHeader("User-Agent", "GreenOps-CI-Analyzer/1.0")
                .build();
    }
}