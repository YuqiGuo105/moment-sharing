package com.example.datalake.backend.dao;

import com.example.datalake.backend.model.Record;

import java.util.List;
import java.util.UUID;

public interface RecordRepository {
    List<Record> findAll();
    Record findById(UUID id);
    Record save(Record record);
    void deleteById(UUID id);
}
