package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.InMemoryRecordRepository;
import com.example.datalake.backend.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RecordServiceTest {

    private RecordService service;
    private Record sampleRecord;

    @BeforeEach
    void setUp() throws IOException {
        InMemoryRecordRepository repo = new InMemoryRecordRepository();
        service = new RecordService(repo);
        ObjectMapper mapper = new ObjectMapper();
        sampleRecord = mapper.readValue(
                getClass().getResourceAsStream("/record.json"),
                Record.class);
    }

    @Test
    void saveAndFind() {
        Record saved = service.save(sampleRecord);
        assertNotNull(service.findById(saved.getId()));
        assertEquals(1, service.findAll().size());
    }
}
