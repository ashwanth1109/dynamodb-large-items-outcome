package com.hackathon.shared;

import lombok.Getter;

public class PerformanceService {
    private long startTime;
    private long endTime;

    @Getter
    private final String markName;

    public PerformanceService(String markName) {
        this.markName = markName;
        start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void end() {
        endTime = System.currentTimeMillis();
    }

    public long measure() {
        return endTime - startTime;
    }
}
