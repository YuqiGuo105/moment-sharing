package com.example.datalake.backend.dto;

import com.example.datalake.backend.model.Record;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for {@link Record}.
 * The identifier is generated automatically when creating a new record,
 * so clients do not need to supply it.
 */
@Data
public class RecordDto {
    private OffsetDateTime createdAt;
    private String url;
    private String owner;

    public static RecordDto fromRecord(Record record) {
        RecordDto dto = new RecordDto();
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUrl(record.getUrl());
        dto.setOwner(record.getOwner());
        return dto;
    }
}
