package com.greenops.gitanalyzer.parser;

import com.greenops.gitanalyzer.model.GitProvider;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class GitUrlParser {

    public ParsedGitUrl parse(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("L'URL du dépôt Git ne peut pas être vide.");
        }

        String trimmed = rawUrl.trim();
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.endsWith(".git")) {
            trimmed = trimmed.substring(0, trimmed.length() - 4);
        }

        URI uri;
        try {
            uri = new URI(trimmed);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Format d'URL invalide : " + rawUrl, e);
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Seules les URLs publiques en HTTPS sont autorisées.");
        }

        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException("Hôte manquant dans l'URL : " + rawUrl);
        }

        GitProvider provider = resolveProvider(host.toLowerCase());
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Chemin du dépôt manquant dans l'URL.");
        }

        String[] segments = path.replaceFirst("^/", "").split("/");
        if (segments.length < 2 || segments[0].isBlank() || segments[1].isBlank()) {
            throw new IllegalArgumentException("L'URL doit contenir le propriétaire et le nom du dépôt (ex: https://github.com/owner/repo).");
        }

        String owner = segments[0];
        String repo = segments[1];
        String normalized = "https://" + host.toLowerCase() + "/" + owner + "/" + repo;

        return new ParsedGitUrl(provider, owner, repo, normalized);
    }

    private GitProvider resolveProvider(String host) {
        if ("github.com".equals(host) || "www.github.com".equals(host)) {
            return GitProvider.GITHUB;
        }
        if ("gitlab.com".equals(host) || "www.gitlab.com".equals(host)) {
            return GitProvider.GITLAB;
        }
        throw new IllegalArgumentException("Fournisseur non supporté : " + host + ". Utilisez github.com ou gitlab.com.");
    }
}