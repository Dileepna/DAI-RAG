package com.dai.microservice.rag.domain;

import java.time.Instant;

public record Measurement(String uid, Instant timestamp, String name, String value, String unit, String source) {
    public Measurement {
        if (uid == null || uid.isBlank() || timestamp == null || name == null || name.isBlank()) {
            throw new IllegalArgumentException("UID, timestamp and measurement name are required");
        }
        value = value == null ? "" : value;
        unit = unit == null ? "" : unit;
        source = source == null ? "" : source;
    }
}
