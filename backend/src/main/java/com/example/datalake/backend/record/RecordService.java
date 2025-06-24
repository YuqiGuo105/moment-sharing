package com.example.datalake.backend.record;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** Service handling CRUD operations for {@link Record} using DTOs. */

@Service
public class RecordService {

    private final RecordRepository repository;

    public RecordService(RecordRepository repository) {
        this.repository = repository;
    }

    public List<RecordDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public RecordDto findById(UUID id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    public RecordDto create(RecordDto dto) {
        Record entity = new Record();
        entity.setUrl(dto.getUrl());
        entity.setOwner(dto.getOwner());
        Record saved = repository.save(entity);
        return toDto(saved);
    }

    public RecordDto update(UUID id, RecordDto dto) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setUrl(dto.getUrl());
                    existing.setOwner(dto.getOwner());
                    return toDto(repository.save(existing));
                })
                .orElse(null);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private RecordDto toDto(Record entity) {
        if (entity == null) {
            return null;
        }
        RecordDto dto = new RecordDto();
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUrl(entity.getUrl());
        dto.setOwner(entity.getOwner());
        return dto;
    }
}
