package com.dai.microservice.rag.infrastructure.adapter.passive.vector;

import com.dai.microservice.rag.application.port.out.VectorIndex;
import com.dai.microservice.rag.domain.MeasurementChunk;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Component
public class QdrantVectorIndex implements VectorIndex {
    private static final Pattern SAFE_UID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private final VectorStore store;

    public QdrantVectorIndex(VectorStore store) { this.store = store; }

    @Override
    public void upsert(List<MeasurementChunk> chunks) {
        store.add(chunks.stream().map(chunk -> new Document(chunk.id(), chunk.content(),
                Map.of("deviceUid", chunk.uid(), "startTime", chunk.start().toString(),
                        "endTime", chunk.end().toString(), "startEpochMillis", chunk.start().toEpochMilli(),
                        "endEpochMillis", chunk.end().toEpochMilli(), "source", chunk.source()))).toList());
    }

    @Override
    public List<MeasurementChunk> search(String query, int topK, SearchFilter filter) {
        var expression = filterExpression(filter);
        var request = SearchRequest.builder().query(query).topK(topK);
        if (!expression.isBlank()) request.filterExpression(expression);
        return store.similaritySearch(request.build()).stream().map(this::toChunk).toList();
    }

    private String filterExpression(SearchFilter filter) {
        var conditions = new ArrayList<String>();
        if (filter.uid() != null && !filter.uid().isBlank()) {
            if (!SAFE_UID.matcher(filter.uid()).matches()) throw new IllegalArgumentException("Invalid deviceUid filter");
            conditions.add("deviceUid == '" + filter.uid() + "'");
        }
        if (filter.from() != null) conditions.add("startEpochMillis >= " + filter.from().toEpochMilli());
        if (filter.to() != null) conditions.add("startEpochMillis <= " + filter.to().toEpochMilli());
        return String.join(" && ", conditions);
    }

    private MeasurementChunk toChunk(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return new MeasurementChunk(document.getId(), String.valueOf(metadata.get("deviceUid")),
                Instant.parse(String.valueOf(metadata.get("startTime"))),
                Instant.parse(String.valueOf(metadata.get("endTime"))),
                String.valueOf(metadata.get("source")), document.getText());
    }
}
