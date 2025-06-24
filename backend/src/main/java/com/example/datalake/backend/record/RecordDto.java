package com.example.datalake.backend.record;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for {@link Record}.
 * The identifier is generated automatically when creating a new record,
 * so clients do not need to supply it.
 */
@Data
public class RecordDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private String url;
    private String owner;

    public static RecordDto fromRecord(Record record) {
        RecordDto dto = new RecordDto();
        dto.setId(record.getId());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUrl(record.getUrl());
        dto.setOwner(record.getOwner());
        return dto;
    }
}
