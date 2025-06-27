package com.example.datalake.backend.model;

import lombok.*;


import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Simple data object representing an uploaded record stored in Firebase.
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
