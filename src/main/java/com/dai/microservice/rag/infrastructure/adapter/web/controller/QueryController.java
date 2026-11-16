package com.dai.microservice.rag.infrastructure.adapter.web.controller;

import com.dai.microservice.rag.application.port.in.QueryMeasurementsUseCase;
import com.dai.microservice.rag.application.port.out.VectorIndex.SearchFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/llm/query")
public class QueryController {
    private final QueryMeasurementsUseCase query;

    public QueryController(QueryMeasurementsUseCase query) {
        this.query = query;
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Query service is up and running.";
    }

    @PostMapping
    public QueryMeasurementsUseCase.Answer query(@Valid @RequestBody QueryRequest request) {
        if (request.from() != null && request.to() != null && request.from().isAfter(request.to())) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        return query.query(request.prompt(), request.topK(),
                new SearchFilter(request.deviceUid(), request.from(), request.to()));
    }

    public record QueryRequest(@NotBlank @Size(max = 4000) String prompt,
            @Min(1) @Max(20) int topK, @Size(max = 128) String deviceUid,
            Instant from, Instant to) { }
}
