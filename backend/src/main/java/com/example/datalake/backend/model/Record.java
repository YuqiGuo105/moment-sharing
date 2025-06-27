package com.example.datalake.backend.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.OffsetDateTime;


import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Simple data object representing an uploaded record stored in Firebase.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collectionName = "records")
public class Record {
    @DocumentId
    private String id;

    private com.google.cloud.Timestamp createdAt;

    @NotBlank(message = "url must not be blank")
    private String url;

    @NotBlank(message = "owner must not be blank")
    private String owner;
}
