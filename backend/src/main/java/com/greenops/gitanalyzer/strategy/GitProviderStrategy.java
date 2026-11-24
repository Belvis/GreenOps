package com.greenops.gitanalyzer.strategy;

import com.greenops.gitanalyzer.dto.CiRunRawData;
import com.greenops.gitanalyzer.model.GitProvider;
import com.greenops.gitanalyzer.parser.ParsedGitUrl;

import java.util.List;

public interface GitProviderStrategy {

    GitProvider getSupportedProvider();

    List<CiRunRawData> fetchRecentPipelineRuns(ParsedGitUrl parsedUrl);
}