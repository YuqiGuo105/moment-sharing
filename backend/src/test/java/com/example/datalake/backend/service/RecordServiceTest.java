package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.model.Record;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RecordServiceTest {

    @Test
    void saveAndLoad() {
        SpringDataRecordRepository repo = mock(SpringDataRecordRepository.class);
        when(repo.save(any(Record.class))).thenAnswer(inv -> inv.getArgument(0));

        RecordService service = new RecordService(repo);

        Record rec = new Record();
        rec.setUrl("http://example.com/file");
        rec.setOwner("alice");

        service.save(rec);

        assertNotNull(rec.getId());
        assertNotNull(rec.getCreatedAt());

        when(repo.findAll()).thenReturn(List.of(rec));
        when(repo.findById(rec.getId())).thenReturn(Optional.of(rec));

        List<Record> all = service.findAll();
        assertEquals(1, all.size());
        Record loaded = service.findById(rec.getId());
        assertNotNull(loaded);
        assertEquals("alice", loaded.getOwner());

        verify(repo).save(any(Record.class));
        verify(repo).findAll();
        verify(repo).findById(rec.getId());
    }
}
