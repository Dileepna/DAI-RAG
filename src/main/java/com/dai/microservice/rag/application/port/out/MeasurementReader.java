package com.dai.microservice.rag.application.port.out;

import com.dai.microservice.rag.domain.Measurement;
import java.io.InputStream;

public interface MeasurementReader {
    void read(InputStream archive, String archiveName, MeasurementHandler handler) throws Exception;

    @FunctionalInterface
    interface MeasurementHandler {
        void accept(Measurement measurement) throws Exception;
    }
}
