package com.dai.microservice.rag.application.port.in;

import java.io.InputStream;

public interface IngestMeasurementsUseCase {
    long ingest(Command command) throws Exception;

    record Command(InputStream archive, String archiveName) { }
}
