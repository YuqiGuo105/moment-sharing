package com.example.datalake.backend.controller;

import com.example.datalake.backend.model.Record;
import com.example.datalake.backend.dto.RecordDto;
import com.example.datalake.backend.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/records")
@Tag(name = "Record", description = "Operations on Record table")
public class RecordController {

    private final RecordService service;

    public RecordController(RecordService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all records")
    public List<RecordDto> list() {
        return service.findAll().stream()
                .map(RecordDto::fromRecord)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a record by id")
    public ResponseEntity<RecordDto> get(@PathVariable UUID id) {
        Record record = service.findById(id);
        return record != null
                ? ResponseEntity.ok(RecordDto.fromRecord(record))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Create a new record")
    public ResponseEntity<RecordDto> create(@RequestBody RecordDto dto) {
        Record record = new Record();
        record.setUrl(dto.getUrl());
        record.setOwner(dto.getOwner());
        Record saved = service.save(record);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RecordDto.fromRecord(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing record")
    public ResponseEntity<RecordDto> update(@PathVariable UUID id, @RequestBody RecordDto dto) {
        Record existing = service.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setUrl(dto.getUrl());
        existing.setOwner(dto.getOwner());
        Record saved = service.save(existing);
        return ResponseEntity.ok(RecordDto.fromRecord(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (service.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
