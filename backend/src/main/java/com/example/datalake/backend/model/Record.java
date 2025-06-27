package com.example.datalake.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Simple data object representing an uploaded record stored in Firebase.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "records")
public class Record {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    private OffsetDateTime createdAt;

    private String url;

    private String owner;
}
