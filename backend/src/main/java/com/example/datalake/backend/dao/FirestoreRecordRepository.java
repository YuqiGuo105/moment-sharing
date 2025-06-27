package com.example.datalake.backend.dao;

import com.example.datalake.backend.model.Record;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "firestore.enabled", havingValue = "true", matchIfMissing = true)
public class FirestoreRecordRepository implements RecordRepository {

    private final CollectionReference collection;

    public FirestoreRecordRepository() {
        Firestore firestore = FirestoreOptions.getDefaultInstance().getService();
        this.collection = firestore.collection("records");
    }

    @Override
    public List<Record> findAll() {
        try {
            return collection.get().get().getDocuments().stream()
                    .map(this::fromDocument)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch records", e);
        }
    }

    @Override
    public Record findById(UUID id) {
        try {
            DocumentSnapshot doc = collection.document(id.toString()).get().get();
            if (!doc.exists()) return null;
            return fromDocument(doc);
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    @Override
    public Record save(Record record) {
        try {
            if (record.getId() == null) {
                record.setId(UUID.randomUUID());
                record.setCreatedAt(OffsetDateTime.now());
            }
            collection.document(record.getId().toString()).set(toMap(record)).get();
            return record;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save record", e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            collection.document(id.toString()).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete record", e);
        }
    }

    private Record fromDocument(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) return null;
        OffsetDateTime createdAt = OffsetDateTime.parse((String) data.get("createdAt"));
        String url = (String) data.get("url");
        String owner = (String) data.get("owner");
        return new Record(UUID.fromString(doc.getId()), createdAt, url, owner);
    }

    private Map<String, Object> toMap(Record record) {
        return Map.of(
                "createdAt", record.getCreatedAt().toString(),
                "url", record.getUrl(),
                "owner", record.getOwner()
        );
    }
}
