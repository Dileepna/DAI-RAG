package com.dai.microservice.rag.domain;

import java.time.Instant;
import java.util.UUID;

public record MeasurementChunk(
        String id, String uid, Instant start, Instant end, String source, String content) {
    public MeasurementChunk {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
