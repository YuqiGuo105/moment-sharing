package com.example.datalake.backend.dto;

import com.example.datalake.backend.model.Record;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for {@link Record}.
 * The identifier is generated automatically when creating a new record,
 * so clients do not need to supply it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordDto {
    @Schema(
            description = "Creation timestamp (ISO-8601, UTC)",
            example     = "2025-06-26T15:04:05Z"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private OffsetDateTime createdAt;

    @Schema(description = "Resource URL", example = "https://example.com")
    private String url;

    @Schema(description = "Owner username", example = "alice")
    private String owner;

    @Schema(description = "Optional description of the record")
    private String description;
}
