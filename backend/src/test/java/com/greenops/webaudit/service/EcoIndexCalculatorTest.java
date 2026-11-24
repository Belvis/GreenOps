
package com.greenops.webaudit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EcoIndexCalculatorTest {

    private EcoIndexCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new EcoIndexCalculator();
    }

    @Test
    void shouldCalculateExcellentScoreForMinimalistPage() {
        var result = calculator.calculate(100, 10, 100);

        assertEquals(90.16, result.score(), 0.01);
        assertEquals('A', result.grade());
        assertEquals(1.20, result.co2Grams(), 0.01);
        assertEquals(1.80, result.waterCl(), 0.01);
    }

    @Test
    void shouldCalculateLowScoreForHeavyPage() {
        var result = calculator.calculate(1500, 100, 1000);

        assertEquals(30.21, result.score(), 0.01);
        assertEquals('E', result.grade());
        assertEquals(2.40, result.co2Grams(), 0.01);
        assertEquals(3.59, result.waterCl(), 0.01);
    }

    @Test
    void shouldReturnGradeAForScoreAbove80() {
        var result = calculator.calculate(100, 10, 100);

        assertEquals('A', result.grade());
    }

    @Test
    void shouldRejectNegativeDomElements() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(-1, 10, 100)
        );
    }

    @Test
    void shouldRejectNegativeRequests() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(100, -1, 100)
        );
    }

    @Test
    void shouldRejectNegativePageSize() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(100, 10, -1)
        );
    }

    @Test
    void shouldReturnMaximumScoreForEmptyPage() {
        var result = calculator.calculate(0, 0, 0);

        assertEquals(100, result.score(), 0.01);
        assertEquals('A', result.grade());
    }
}
