package com.dai.microservice.rag.application.port.out;

import com.dai.microservice.rag.domain.MeasurementChunk;
import java.util.List;

public interface MetadataStore {
    void save(List<MeasurementChunk> chunks);
}
