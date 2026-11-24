
package com.greenops.webaudit.service;

import org.springframework.stereotype.Service;

@Service
public class EcoIndexCalculator {

    private static final double[] DOM_QUANTILES = {
            0, 47, 75, 159, 233, 298, 358, 417, 476, 537,
            603, 674, 753, 843, 949, 1076, 1237, 1459,
            1801, 2479, 594601
    };

    private static final double[] REQUEST_QUANTILES = {
            0, 2, 15, 25, 34, 42, 49, 56, 63, 70,
            78, 86, 95, 105, 117, 130, 147, 170,
            205, 281, 3920
    };

    private static final double[] SIZE_QUANTILES = {
            0, 1.37, 144.7, 319.53, 479.46, 631.97,
            783.38, 937.91, 1098.62, 1265.47,
            1448.32, 1648.27, 1876.08, 2142.06,
            2465.37, 2866.31, 3401.59, 4155.73,
            5400.08, 8037.54, 223212.26
    };

    public EcoIndexResult calculate(
            int totalElements,
            int totalRequests,
            double pageSizeKb
    ) {
        if (totalElements < 0 || totalRequests < 0
                || !Double.isFinite(pageSizeKb) || pageSizeKb < 0) {
            throw new IllegalArgumentException(
                    "Les métriques doivent être positives ou nulles."
            );
        }

        double qDom = calculateQuantile(totalElements, DOM_QUANTILES);
        double qReq = calculateQuantile(totalRequests, REQUEST_QUANTILES);
        double qSize = calculateQuantile(pageSizeKb, SIZE_QUANTILES);

        double score = 100 - (
                5 * (3 * qDom + 2 * qReq + qSize)
        ) / 6;

        char grade = calculateGrade(score);

        double co2Grams = 2 + (2 * (50 - score)) / 100;
        double waterCl = 3 + (3 * (50 - score)) / 100;

        return new EcoIndexResult(
                round(score),
                grade,
                round(co2Grams),
                round(waterCl)
        );
    }

    private double calculateQuantile(double value, double[] bounds) {
        for (int i = 1; i < bounds.length; i++) {
            if (value < bounds[i]) {
                double lower = bounds[i - 1];
                double upper = bounds[i];

                return (i - 1) + (value - lower) / (upper - lower);
            }
        }

        return 20;
    }

    private char calculateGrade(double score) {
        if (score >= 80) return 'A';
        if (score >= 70) return 'B';
        if (score >= 55) return 'C';
        if (score >= 40) return 'D';
        if (score >= 25) return 'E';
        if (score >= 10) return 'F';
        return 'G';
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record EcoIndexResult(
            double score,
            char grade,
            double co2Grams,
            double waterCl
    ) {}
}
