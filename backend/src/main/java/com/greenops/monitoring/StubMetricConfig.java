package com.greenops.monitoring;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("monitoring-stub")
public class StubMetricConfig {

    @Bean
    MetricProvider stubWebProvider() { return new StubProvider(TargetType.WEB, true, 40, 95); }

    @Bean
    MetricProvider stubGitProvider() { return new StubProvider(TargetType.GIT, false, 50, 200); }

    static class StubProvider implements MetricProvider {
        private final TargetType type;
        private final boolean higherIsBetter;
        private final int min, max;

        StubProvider(TargetType type, boolean higherIsBetter, int min, int max) {
            this.type = type; this.higherIsBetter = higherIsBetter; this.min = min; this.max = max;
        }
        @Override public TargetType supports() { return type; }
        @Override public double fetchCurrentScore(String targetId) {
            return ThreadLocalRandom.current().nextInt(min, max + 1);
        }
        @Override public boolean higherIsBetter() { return higherIsBetter; }
    }
}
