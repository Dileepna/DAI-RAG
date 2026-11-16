package com.dai.microservice.rag.application.service;

import com.dai.microservice.rag.application.port.in.QueryMeasurementsUseCase;
import com.dai.microservice.rag.application.port.out.ChatGateway;
import com.dai.microservice.rag.application.port.out.VectorIndex;
import com.dai.microservice.rag.domain.MeasurementChunk;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class QueryService implements QueryMeasurementsUseCase {
    private final VectorIndex vectors;
    private final ChatGateway chat;
    private final int maxContextChars;

    @Override
    public Answer query(String prompt, int topK, VectorIndex.SearchFilter filter) {
        var matches = vectors.search(prompt, topK, filter);
        var context = matches.stream().map(MeasurementChunk::content).collect(Collectors.joining("\n---\n"));
        if (context.length() > maxContextChars) context = context.substring(0, maxContextChars);
        return new Answer(chat.answer(prompt, context), matches.stream().map(MeasurementChunk::id).toList());
    }
}
