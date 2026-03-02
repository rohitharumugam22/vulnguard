package com.rohith.vulnguard.model.enums;

public enum Severity {
    CRITICAL(10.0),
    HIGH(7.0),
    MEDIUM(4.0),
    LOW(1.0),
    INFO(0.5);

    private final double weight;

    Severity(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
