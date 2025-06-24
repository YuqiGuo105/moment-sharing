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
@Tag(name = "Record", description = "Operations on Record table")
public class RecordController {

    private final RecordService service;

    public RecordController(RecordService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all records")
    public List<RecordDto> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a record by id")
    public ResponseEntity<RecordDto> get(@PathVariable UUID id) {
        RecordDto record = service.findById(id);
        return record != null ? ResponseEntity.ok(record) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Create a new record")
    public ResponseEntity<RecordDto> create(@RequestBody RecordDto record) {
        RecordDto saved = service.create(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing record")
    public ResponseEntity<RecordDto> update(@PathVariable UUID id, @RequestBody RecordDto record) {
        if (service.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        RecordDto saved = service.update(id, record);
        return ResponseEntity.ok(saved);
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
