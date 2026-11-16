package com.dai.microservice.rag.infrastructure.adapter.passive.persistence;

import com.dai.microservice.rag.domain.MeasurementChunk;
import com.dai.microservice.rag.application.port.out.MetadataStore;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaMetadataStore implements MetadataStore {
    private final ChunkMetadataRepository repository;

    public JpaMetadataStore(ChunkMetadataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(List<MeasurementChunk> chunks) {
        repository.saveAll(chunks.stream().map(chunk -> new ChunkMetadata(chunk.id(), chunk.uid(),
                chunk.source(), chunk.start(), chunk.end())).toList());
    }
}
