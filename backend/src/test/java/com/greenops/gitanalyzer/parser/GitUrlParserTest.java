package com.greenops.gitanalyzer.parser;

import com.greenops.gitanalyzer.model.GitProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitUrlParserTest {

    private final GitUrlParser parser = new GitUrlParser();

    @Test
    void shouldParseValidGitHubUrlWithSuffixes() {
        ParsedGitUrl result = parser.parse("  https://github.com/Belvis/GreenOps.git/  ");

        assertEquals(GitProvider.GITHUB, result.provider());
        assertEquals("Belvis", result.owner());
        assertEquals("GreenOps", result.repo());
        assertEquals("Belvis/GreenOps", result.fullName());
        assertEquals("https://github.com/Belvis/GreenOps", result.normalizedUrl());
    }

    @Test
    void shouldParseValidGitLabUrl() {
        ParsedGitUrl result = parser.parse("https://gitlab.com/gitlab-org/gitlab");

        assertEquals(GitProvider.GITLAB, result.provider());
        assertEquals("gitlab-org", result.owner());
        assertEquals("gitlab", result.repo());
    }

    @Test
    void shouldRejectNonHttpsUrl() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("http://github.com/Belvis/GreenOps")
        );
        assertTrue(ex.getMessage().contains("HTTPS"));
    }

    @Test
    void shouldRejectUnsupportedHost() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("https://bitbucket.org/team/repo")
        );
    }

    @Test
    void shouldRejectUrlWithoutRepoSegment() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("https://github.com/Belvis")
        );
    }

    @Test
    void shouldRejectBlankOrNullUrl() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }
}