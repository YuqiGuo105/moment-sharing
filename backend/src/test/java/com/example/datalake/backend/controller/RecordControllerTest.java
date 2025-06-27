package com.example.datalake.backend.controller;

import com.example.datalake.backend.dto.RecordDto;
import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.model.Record;
import com.example.datalake.backend.service.RecordService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RecordControllerTest {

    @Test
    void createAndGet() {
        SpringDataRecordRepository repo = mock(SpringDataRecordRepository.class);
        when(repo.save(any(Record.class))).thenAnswer(inv -> inv.getArgument(0));

        RecordService service = new RecordService(repo);
        RecordController controller = new RecordController(service);

        RecordDto dto = new RecordDto();
        dto.setUrl("http://example.com");
        dto.setOwner("bob");
        ResponseEntity<RecordDto> created = controller.create(dto);
        assertEquals(201, created.getStatusCode().value());

        Record saved = new Record();
        saved.setId(UUID.randomUUID());
        saved.setOwner(dto.getOwner());
        saved.setUrl(dto.getUrl());
        when(repo.findAll()).thenReturn(List.of(saved));
        when(repo.findById(saved.getId())).thenReturn(Optional.of(saved));

        UUID id = saved.getId();
        ResponseEntity<RecordDto> fetched = controller.get(id);
        assertEquals(200, fetched.getStatusCode().value());
        assertEquals("bob", fetched.getBody().getOwner());

        verify(repo).save(any(Record.class));
        verify(repo).findById(saved.getId());
    }
}
