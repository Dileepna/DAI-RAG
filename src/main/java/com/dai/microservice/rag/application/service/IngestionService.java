package com.dai.microservice.rag.application.service;

import com.dai.microservice.rag.application.port.in.IngestMeasurementsUseCase;
import com.dai.microservice.rag.application.port.out.MeasurementReader;
import com.dai.microservice.rag.application.port.out.MetadataStore;
import com.dai.microservice.rag.application.port.out.VectorIndex;
import com.dai.microservice.rag.domain.ChunkingEngine;
import com.dai.microservice.rag.domain.MeasurementChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public final class IngestionService implements IngestMeasurementsUseCase {
    private final MeasurementReader reader;
    private final VectorIndex vectors;
    private final MetadataStore metadata;
    private final int batchSize;
    private final int maxMeasurementsPerChunk;


    @Override
    public long ingest(Command command) throws Exception {
        var chunker = new ChunkingEngine(maxMeasurementsPerChunk);
        var batch = new ArrayList<MeasurementChunk>(batchSize);
        long[] total = {0};
        reader.read(command.archive(), command.archiveName(), measurement -> {
            chunker.accept(measurement).ifPresent(batch::add);
            if (batch.size() >= batchSize) {
                persist(batch);
                total[0] += batch.size();
                batch.clear();
            }
        });
        chunker.finishIfNotEmpty().ifPresent(batch::add);
        if (!batch.isEmpty()) {
            persist(batch);
            total[0] += batch.size();
        }
        return total[0];
    }

    private void persist(List<MeasurementChunk> batch) {
        vectors.upsert(batch);
        metadata.save(List.copyOf(batch));
    }
}
