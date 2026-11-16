package com.dai.microservice.rag.application.port.out;

import com.dai.microservice.rag.domain.MeasurementChunk;
import java.util.List;

public interface VectorIndex {
    void upsert(List<MeasurementChunk> chunks);
    List<MeasurementChunk> search(String query, int topK, SearchFilter filter);

    record SearchFilter(String uid, java.time.Instant from, java.time.Instant to) { }
}
