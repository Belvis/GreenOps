package com.greenops.monitoring;

public record DriftResult(double deltaPercent, boolean alert, boolean gradeDropped) {}
