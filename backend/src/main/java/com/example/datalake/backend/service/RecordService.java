package com.example.datalake.backend.service;

import com.example.datalake.backend.model.Record;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RecordService {

    protected final Firestore firestore;

    public RecordService() {
        this(FirestoreClient.getFirestore());
    }

    public RecordService(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Record> findAll() {
        try {
            ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = firestore.collection("records").get();
            return future.get().getDocuments().stream()
                    .map(d -> d.toObject(Record.class))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Record findById(UUID id) {
        try {
            DocumentReference ref = firestore.collection("records").document(id.toString());
            return ref.get().get().toObject(Record.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Record save(Record record) {
        try {
            if (record.getId() == null) {
                record.setId(UUID.randomUUID());
            }
            firestore.collection("records").document(record.getId().toString()).set(record).get();
            return record;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteById(UUID id) {
        try {
            firestore.collection("records").document(id.toString()).delete().get();
        } catch (Exception ignored) {
        }
    }
}
