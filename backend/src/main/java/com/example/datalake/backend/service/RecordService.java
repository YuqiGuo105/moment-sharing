package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.RecordRepository;
import com.example.datalake.backend.model.Record;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RecordService {

    private final RecordRepository repository;

    public RecordService(RecordRepository repository) {
        this.repository = repository;
    }

    public List<Record> findAll() {
        return repository.findAll();
    }

    public Record findById(UUID id) {
        return repository.findById(id);
    }

    public Record save(Record record) {
        return repository.save(record);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
