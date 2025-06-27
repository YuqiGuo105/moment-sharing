package com.example.datalake.backend.dao;

import com.example.datalake.backend.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RecordRepositoryTest {

    private InMemoryRecordRepository repository;
    private Record sampleRecord;

    @BeforeEach
    void setUp() throws IOException {
        repository = new InMemoryRecordRepository();
        ObjectMapper mapper = new ObjectMapper();
        sampleRecord = mapper.readValue(
                getClass().getResourceAsStream("/record.json"),
                Record.class);
    }

    @Test
    void saveAssignsIdAndPersists() {
        Record saved = repository.save(sampleRecord);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("http://example.com/test.jpg", saved.getUrl());
        assertEquals("alice", saved.getOwner());
        assertEquals(saved, repository.findById(saved.getId()));
    }

    @Test
    void deleteRemovesRecord() {
        Record saved = repository.save(sampleRecord);
        repository.deleteById(saved.getId());
        assertNull(repository.findById(saved.getId()));
    }
}
