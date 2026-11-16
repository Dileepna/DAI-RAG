package com.dai.microservice.rag.infrastructure.adapter.passive.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "measurement_chunks")
public class ChunkMetadata {
    @Id
    private String id;
    @Column(name = "device_uid", nullable = false, length = 128)
    private String deviceUid;
    @Column(nullable = false, length = 512)
    private String source;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    protected ChunkMetadata() { }

    public ChunkMetadata(String id, String deviceUid, String source, Instant startTime, Instant endTime) {
        this.id = id;
        this.deviceUid = deviceUid;
        this.source = source;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
