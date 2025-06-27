package com.example.datalake.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data model for a Firestore record.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record {
    private UUID id;
    private OffsetDateTime createdAt;
    private String url;
    private String owner;
}
