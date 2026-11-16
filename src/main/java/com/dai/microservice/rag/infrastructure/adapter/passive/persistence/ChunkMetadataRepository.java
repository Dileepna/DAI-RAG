package com.dai.microservice.rag.infrastructure.adapter.passive.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkMetadataRepository extends JpaRepository<ChunkMetadata, String> { }
