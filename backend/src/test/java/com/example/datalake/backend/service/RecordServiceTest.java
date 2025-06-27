package com.example.datalake.backend.service;

import com.example.datalake.backend.model.Record;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecordServiceTest {

    @TempDir
    Path temp;

    @Test
    void saveAndLoad() throws Exception {
        Path file = temp.resolve("records.json");
        FileRecordService service = new FileRecordService(file);

        Record rec = new Record();
        rec.setUrl("http://example.com/file");
        rec.setOwner("alice");
        service.save(rec);

        List<Record> all = service.findAll();
        assertEquals(1, all.size());
        Record loaded = service.findById(rec.getId());
        assertNotNull(loaded);
        assertEquals("alice", loaded.getOwner());
    }
}
