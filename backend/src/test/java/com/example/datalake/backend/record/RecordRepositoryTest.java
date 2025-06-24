package com.example.datalake.backend.record;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/records.sql")
class RecordRepositoryTest {

    @Autowired
    private RecordRepository repository;

    @Test
    void findAllReturnsInsertedRows() {
        List<Record> records = repository.findAll();
        assertThat(records).hasSize(2);
    }

    @Test
    void findByIdReturnsCorrectRow() {
        Optional<Record> record = repository.findById(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(record).isPresent();
        assertThat(record.get().getOwner()).isEqualTo("alice");
    }

    @Test
    void savePersistsEntity() {
        Record r = new Record();
        r.setUrl("http://example.com/3");
        r.setOwner("carol");
        Record saved = repository.save(r);
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findAll()).hasSize(3);
    }

    @Test
    void deleteByIdRemovesRow() {
        repository.deleteById(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(repository.findAll()).hasSize(1);
    }
}
