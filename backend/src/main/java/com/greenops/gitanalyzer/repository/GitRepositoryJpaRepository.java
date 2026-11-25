package com.greenops.gitanalyzer.repository;

import com.greenops.gitanalyzer.entity.GitRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GitRepositoryJpaRepository extends JpaRepository<GitRepository, Long> {
    Optional<GitRepository> findByNormalizedUrl(String normalizedUrl);
    List<GitRepository> findAllByOrderByLastAnalyzedAtDesc();
}