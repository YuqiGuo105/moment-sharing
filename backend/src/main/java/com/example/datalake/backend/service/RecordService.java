package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.dto.RecordDto;
import com.example.datalake.backend.model.Record;
import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Flux<Record> findByOwner(String owner) {
        return repo.findByOwner(owner);
    }

    /* ---------- CREATE (DTO → Entity) ---------- */
    public Mono<Record> create(RecordDto dto) {
        Record entity = toEntity(dto);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Timestamp.now());
        }
        return repo.findByOwner(entity.getOwner())
                .flatMap(existing -> repo.deleteById(existing.getId()))
                .then(repo.save(entity));
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
        r.setCreatedAt(dto.getCreatedAt() != null
                ? Timestamp.ofTimeSecondsAndNanos(
                dto.getCreatedAt().toEpochSecond(), dto.getCreatedAt().getNano())
                : Timestamp.now());
        r.setUrl(dto.getUrl());
        r.setOwner(dto.getOwner());
        r.setDescription(dto.getDescription());
        return r;
    }

}
