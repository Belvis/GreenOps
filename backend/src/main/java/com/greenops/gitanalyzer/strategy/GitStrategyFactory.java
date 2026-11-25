package com.greenops.gitanalyzer.strategy;

import com.greenops.gitanalyzer.model.GitProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class GitStrategyFactory {

    private final Map<GitProvider, GitProviderStrategy> strategies = new EnumMap<>(GitProvider.class);

    public GitStrategyFactory(List<GitProviderStrategy> strategyList) {
        for (GitProviderStrategy strategy : strategyList) {
            strategies.put(strategy.getSupportedProvider(), strategy);
        }
    }

    public GitProviderStrategy getStrategy(GitProvider provider) {
        GitProviderStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("Aucune stratégie configurée pour le fournisseur : " + provider);
        }
        return strategy;
    }
}