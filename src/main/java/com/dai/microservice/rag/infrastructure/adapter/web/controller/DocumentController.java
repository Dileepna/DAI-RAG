package com.dai.microservice.rag.infrastructure.adapter.web.controller;

import com.dai.microservice.rag.application.port.in.IngestMeasurementsUseCase;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/llm/documents")
@Validated
public class DocumentController {
    private final IngestMeasurementsUseCase ingestion;

    public DocumentController(IngestMeasurementsUseCase ingestion) {
        this.ingestion = ingestion;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestPart("file") @NotNull MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("ZIP file must not be empty");
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Upload must be a ZIP file");
        }
        var command = new IngestMeasurementsUseCase.Command(file.getInputStream(), file.getOriginalFilename());
        return new UploadResponse(ingestion.ingest(command));
    }

    public record UploadResponse(long chunksStored) { }
}
