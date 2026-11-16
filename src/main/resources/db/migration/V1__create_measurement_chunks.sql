CREATE TABLE IF NOT EXISTS measurement_chunks (
    id VARCHAR(36) PRIMARY KEY,
    device_uid VARCHAR(128) NOT NULL,
    source VARCHAR(512) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_measurement_chunks_uid_time
    ON measurement_chunks (device_uid, start_time);
