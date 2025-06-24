package com.example.datalake.backend.record;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Simple DTO for transferring Record data.
 */
@Data
public class RecordDto {
    private OffsetDateTime createdAt;
    private String url;
    private String owner;
}
