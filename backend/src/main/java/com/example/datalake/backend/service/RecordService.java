package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.model.Record;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

@Service
public class RecordService {

    protected final SpringDataRecordRepository repository;

    public RecordService(SpringDataRecordRepository repository) {
        this.repository = repository;
    }

    public List<Record> findAll() {
        return repository.findAll();
    }

    public Record findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public Record save(Record record) {
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(OffsetDateTime.now());
        }
        return repository.save(record);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
