package com.dai.microservice.rag.domain;

import com.dai.microservice.rag.domain.MeasurementChunk;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class ChunkingEngine {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ISO_LOCAL_DATE;
    private final int maxMeasurements;
    private String uid;
    private String source;
    private LocalDate day;
    private Instant start;
    private Instant end;
    private int count;
    private int numericCount;
    private double sum;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;
    private final StringBuilder rows = new StringBuilder();

    public ChunkingEngine(int maxMeasurements) {
        if (maxMeasurements < 1) throw new IllegalArgumentException("maxMeasurements must be positive");
        this.maxMeasurements = maxMeasurements;
    }

    public Optional<MeasurementChunk> accept(Measurement measurement) {
        LocalDate nextDay = measurement.timestamp().atZone(ZoneOffset.UTC).toLocalDate();
        Optional<MeasurementChunk> completed = Optional.empty();
        if (count > 0 && (!measurement.uid().equals(uid) || !nextDay.equals(day)
                || count == maxMeasurements)) {
            completed = Optional.of(finish());
        }
        if (count == 0) {
            uid = measurement.uid();
            source = measurement.source();
            day = nextDay;
            start = measurement.timestamp();
            end = measurement.timestamp();
        }
        if (measurement.timestamp().isBefore(start)) start = measurement.timestamp();
        if (measurement.timestamp().isAfter(end)) end = measurement.timestamp();
        rows.append(measurement.timestamp()).append(" | ").append(measurement.name()).append(" = ")
                .append(measurement.value()).append(measurement.unit()).append('\n');
        try {
            double value = Double.parseDouble(measurement.value());
            sum += value;
            numericCount++;
            min = Math.min(min, value);
            max = Math.max(max, value);
        } catch (NumberFormatException ignored) {
            // Non-numeric measurements remain in the chunk but do not affect its numeric summary.
        }
        count++;
        return completed;
    }

    public Optional<MeasurementChunk> finishIfNotEmpty() {
        return count == 0 ? Optional.empty() : Optional.of(finish());
    }

    private MeasurementChunk finish() {
        String summary = count + " readings";
        if (min != Double.POSITIVE_INFINITY) {
            summary += ", min=" + min + ", max=" + max + ", mean=" + (sum / numericCount);
        }
        var chunk = new MeasurementChunk(null, uid, start, end, source,
                "Device UID: " + uid + "\nUTC day: " + DAY.format(day) + "\n"
                        + rows + "Summary: " + summary);
        uid = source = null;
        day = null;
        start = end = null;
        count = 0;
        numericCount = 0;
        sum = 0;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        rows.setLength(0);
        return chunk;
    }
}
