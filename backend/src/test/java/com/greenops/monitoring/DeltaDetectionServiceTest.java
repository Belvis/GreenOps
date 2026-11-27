package com.greenops.monitoring;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DeltaDetectionServiceTest {

    private final DeltaDetectionService service = new DeltaDetectionService();

    @Test
    void computeDeltaFollowsFormula() {
        assertEquals(-25.0, service.computeDelta(80, 60), 0.001);
        assertEquals(50.0, service.computeDelta(100, 150), 0.001);
    }

    @Test
    void computeDeltaHandlesZeroPrevious() {
        assertEquals(0.0, service.computeDelta(0, 0), 0.001);
        assertEquals(100.0, service.computeDelta(0, 5), 0.001);
    }

    @Test
    void noAlertWhenDropIsBelowThreshold() {
        DriftResult r = service.evaluate(80, 78, 10, true);
        assertFalse(r.alert());
        assertFalse(r.gradeDropped());
    }

    @Test
    void alertWhenCriticalDegradation() {
        DriftResult r = service.evaluate(80, 60, 10, true);
        assertTrue(r.alert());
        assertEquals(-25.0, r.deltaPercent(), 0.001);
    }

    @Test
    void noAlertWhenScoreImproves() {
        assertFalse(service.evaluate(60, 80, 10, true).alert());
    }

    @Test
    void alertWhenEcoIndexGradeDropsEvenBelowThreshold() {
        DriftResult r = service.evaluate(71, 70, 50, true); // B -> C
        assertTrue(r.alert());
        assertTrue(r.gradeDropped());
    }

    @Test
    void lowerIsBetterMetricAlertsOnIncrease() {
        assertTrue(service.evaluate(100, 130, 10, false).alert());
        assertFalse(service.evaluate(100, 70, 10, false).alert());
    }

    @Test
    void gradeLetterFollowsEcoIndexGrid() {
        assertEquals("A", DeltaDetectionService.gradeLetter(95));
        assertEquals("B", DeltaDetectionService.gradeLetter(80));
        assertEquals("C", DeltaDetectionService.gradeLetter(60));
        assertEquals("D", DeltaDetectionService.gradeLetter(45));
        assertEquals("E", DeltaDetectionService.gradeLetter(30));
        assertEquals("F", DeltaDetectionService.gradeLetter(15));
        assertEquals("G", DeltaDetectionService.gradeLetter(5));
    }
}
