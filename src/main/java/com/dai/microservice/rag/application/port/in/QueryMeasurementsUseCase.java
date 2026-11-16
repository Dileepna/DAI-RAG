package com.dai.microservice.rag.application.port.in;

import com.dai.microservice.rag.application.port.out.VectorIndex.SearchFilter;
import java.util.List;

public interface QueryMeasurementsUseCase {
    Answer query(String prompt, int topK, SearchFilter filter);
    record Answer(String answer, List<String> chunkIds) { }
}
