package com.example.datalake.backend.dao;

import com.example.datalake.backend.model.Record;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "firestore.enabled", havingValue = "false")
public class InMemoryRecordRepository implements RecordRepository {

    private final Map<UUID, Record> store = new LinkedHashMap<>();

    @Override
    public List<Record> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Record findById(UUID id) {
        return store.get(id);
    }

    @Override
    public Record save(Record record) {
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
            record.setCreatedAt(OffsetDateTime.now());
        }
        store.put(record.getId(), record);
        return record;
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }
}
