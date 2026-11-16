package com.dai.microservice.rag.config;

import com.dai.microservice.rag.application.port.in.IngestMeasurementsUseCase;
import com.dai.microservice.rag.application.port.in.QueryMeasurementsUseCase;
import com.dai.microservice.rag.application.port.out.ChatGateway;
import com.dai.microservice.rag.application.port.out.MeasurementReader;
import com.dai.microservice.rag.application.port.out.MetadataStore;
import com.dai.microservice.rag.application.port.out.VectorIndex;
import com.dai.microservice.rag.application.service.IngestionService;
import com.dai.microservice.rag.application.service.QueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    IngestMeasurementsUseCase ingestMeasurementsUseCase(MeasurementReader reader, VectorIndex vectors,
            MetadataStore metadata, @Value("${app.ingestion.batch-size:32}") int batchSize,
            @Value("${app.ingestion.max-measurements-per-chunk:100}") int chunkSize) {
        return new IngestionService(reader, vectors, metadata, batchSize, chunkSize);
    }

    @Bean
    QueryMeasurementsUseCase queryMeasurementsUseCase(VectorIndex vectors, ChatGateway chat,
            @Value("${app.query.max-context-chars:24000}") int maxContextChars) {
        return new QueryService(vectors, chat, maxContextChars);
    }
}
