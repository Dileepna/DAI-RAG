package com.dai.microservice.rag.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ChunkingEngineTest {
    @Test
    void splitsByDeviceDayAndConfiguredSize() {
        var engine = new ChunkingEngine(2);
        assertTrue(engine.accept(measurement("A", "2026-01-01T10:00:00Z", "2")).isEmpty());
        assertTrue(engine.accept(measurement("A", "2026-01-01T10:01:00Z", "4")).isEmpty());
        var first = engine.accept(measurement("B", "2026-01-01T10:02:00Z", "6")).orElseThrow();
        assertEquals("A", first.uid());
        assertTrue(first.content().contains("mean=3.0"));
        assertEquals("B", engine.finishIfNotEmpty().orElseThrow().uid());
    }

    private Measurement measurement(String uid, String time, String value) {
        return new Measurement(uid, Instant.parse(time), "temperature", value, "C", "test.xlsx");
    }
}
