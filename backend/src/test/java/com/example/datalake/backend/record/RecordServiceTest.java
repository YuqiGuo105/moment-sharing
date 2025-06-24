package com.example.datalake.backend.record;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/records.sql")
class RecordServiceTest {

    @Autowired
    private RecordService service;

    @Test
    void findAllReturnsDtos() {
        List<RecordDto> records = service.findAll();
        assertThat(records).hasSize(2);
    }

    @Test
    void createAddsRecord() {
        RecordDto dto = new RecordDto();
        dto.setUrl("http://example.com/3");
        dto.setOwner("carol");
        RecordDto saved = service.create(dto);
        assertThat(saved).isNotNull();
        assertThat(service.findAll()).hasSize(3);
    }

    @Test
    void findByIdReturnsDto() {
        RecordDto record = service.findById(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(record).isNotNull();
        assertThat(record.getOwner()).isEqualTo("alice");
    }

    @Test
    void updateChangesRecord() {
        RecordDto dto = new RecordDto();
        dto.setUrl("http://example.com/new");
        dto.setOwner("bob");
        RecordDto updated = service.update(UUID.fromString("22222222-2222-2222-2222-222222222222"), dto);
        assertThat(updated.getUrl()).isEqualTo("http://example.com/new");
    }

    @Test
    void deleteRemovesRecord() {
        service.deleteById(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(service.findAll()).hasSize(1);
    }
}
