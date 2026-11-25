package com.greenops.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemCoefficientRepository extends JpaRepository<SystemCoefficient, Long> {
    Optional<SystemCoefficient> findByParamKey(String paramKey);
}