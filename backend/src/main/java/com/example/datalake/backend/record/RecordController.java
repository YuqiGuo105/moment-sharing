package com.example.datalake.backend.record;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/records")
@Tag(name = "Record API")
public class RecordController {

    private final RecordRepository repository;

    public RecordController(RecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List all records")
    public List<Record> all() {
        return repository.findAll();
    }

    @PostMapping
    @Operation(summary = "Create new record")
    public Record create(@RequestBody Record record) {
        return repository.save(record);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get record by id")
    public ResponseEntity<Record> get(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update record")
    public ResponseEntity<Record> update(@PathVariable UUID id, @RequestBody Record updated) {
        return repository.findById(id)
                .map(record -> {
                    record.setUrl(updated.getUrl());
                    record.setOwner(updated.getOwner());
                    return ResponseEntity.ok(repository.save(record));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
