package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.dto.RecordDto;
import com.example.datalake.backend.model.Record;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final SpringDataRecordRepository repo;

    /* ---------- READ ---------- */
    public Flux<Record> findAll() {
        return repo.findAll();
    }

    public Mono<Record> findById(String id) {
        return repo.findById(id);
    }

    /* ---------- CREATE (DTO → Entity) ---------- */
    public Mono<Record> create(RecordDto dto) {
        Record entity = toEntity(dto);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(OffsetDateTime.now());
        }
        return repo.save(entity);
    }

    /* ---------- UPDATE ---------- */
    public Mono<Record> update(String id, Record updated) {
        updated.setId(id);
        return repo.save(updated);
    }

    /* ---------- DELETE ---------- */
    public Mono<Void> deleteById(String id) {
        return repo.deleteById(id);
    }

    /* ---------- Mapping helper ---------- */
    private Record toEntity(RecordDto dto) {
        Record r = new Record();
        r.setCreatedAt(dto.getCreatedAt());
        r.setUrl(dto.getUrl());
        r.setOwner(dto.getOwner());
        return r;
    }
}
