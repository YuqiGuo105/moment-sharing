package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.dto.RecordDto;
import com.example.datalake.backend.model.Record;
import com.example.datalake.backend.service.StorageService;
import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

    private final SpringDataRecordRepository repo;
    private final StorageService storageService;

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
                .flatMap(existing ->
                        storageService.deleteObjectByUrl(existing.getUrl())
                                .onErrorResume(e -> {
                                    log.warn("Failed to delete file {}", existing.getUrl(), e);
                                    return Mono.empty();
                                })
                                .then(repo.deleteById(existing.getId()))
                )
                .then(repo.save(entity));
    }

    /* ---------- UPDATE ---------- */
    public Mono<Record> update(String id, Record updated, String username) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(existing -> {
                    if (!existing.getOwner().equals(username)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
                    }
                    updated.setId(id);
                    updated.setOwner(username);
                    return repo.save(updated);
                });
    }

    /* ---------- DELETE ---------- */
    public Mono<Void> deleteById(String id, String username) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(existing -> {
                    if (!existing.getOwner().equals(username)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
                    }
                    return repo.deleteById(id);
                });
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
