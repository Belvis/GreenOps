package com.greenops.gitanalyzer.parser;

import com.greenops.gitanalyzer.model.GitProvider;

public record ParsedGitUrl(
        GitProvider provider,
        String owner,
        String repo,
        String normalizedUrl
) {
    public String fullName() {
        return owner + "/" + repo;
    }
}