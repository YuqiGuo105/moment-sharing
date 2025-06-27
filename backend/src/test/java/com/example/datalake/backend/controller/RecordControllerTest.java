package com.example.datalake.backend.controller;

import com.example.datalake.backend.dto.RecordDto;
import com.example.datalake.backend.service.FileRecordService;
import com.example.datalake.backend.service.RecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RecordControllerTest {

    @TempDir
    Path temp;

    @Test
    void createAndGet() throws Exception {
        RecordService service = new FileRecordService(temp.resolve("records.json"));
        RecordController controller = new RecordController(service);

        RecordDto dto = new RecordDto();
        dto.setUrl("http://example.com");
        dto.setOwner("bob");
        ResponseEntity<RecordDto> created = controller.create(dto);
        assertEquals(201, created.getStatusCode().value());

        UUID id = service.findAll().get(0).getId();
        ResponseEntity<RecordDto> fetched = controller.get(id);
        assertEquals(200, fetched.getStatusCode().value());
        assertEquals("bob", fetched.getBody().getOwner());
    }
}
