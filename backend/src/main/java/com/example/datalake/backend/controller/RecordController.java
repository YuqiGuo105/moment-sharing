package com.example.datalake.backend.controller;

import com.example.datalake.backend.model.Record;
import com.example.datalake.backend.dto.RecordDto;
import com.example.datalake.backend.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Tag(name = "Record", description = "CRUD operations for Record documents")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService service;

    /* ---------- READ ---------- */

    @Operation(summary = "List all records")
    @GetMapping
    public Flux<Record> all() {
        return service.findAll();
    }

    @Operation(summary = "List records by owner")
    @GetMapping("/owner/{owner}")
    public Flux<Record> byOwner(
            @Parameter(description = "Owner username")
            @PathVariable String owner) {
        return service.findByOwner(owner);
    }

    @Operation(summary = "Find a record by ID")
    @ApiResponse(responseCode = "200", description = "Found",
            content = @Content(schema = @Schema(implementation = Record.class)))
    @GetMapping("/{id}")
    public Mono<Record> get(@Parameter(description = "Firestore document ID") @PathVariable String id) {
        return service.findById(id);
    }

    /* ---------- CREATE (RecordDto IN, Record OUT) ---------- */

    @Operation(summary = "Create a new record (ID auto-generated)")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = Record.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Record> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = RecordDto.class)))
            @Valid @RequestBody RecordDto in,
            Authentication authentication) {
        in.setOwner(authentication.getName());
        return service.create(in);
    }

    /* ---------- UPDATE (Record IN) ---------- */

    @Operation(summary = "Update an existing record by ID")
    @PutMapping("/{id}")
    public Mono<Record> update(
            @Parameter(description = "Firestore document ID") @PathVariable String id,
            @Valid @RequestBody Record in,
            Authentication authentication) {
        return service.update(id, in, authentication.getName());
    }

    /* ---------- DELETE ---------- */

    @Operation(summary = "Delete a record by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@Parameter(description = "Firestore document ID") @PathVariable String id,
                             Authentication authentication) {
        return service.deleteById(id, authentication.getName());
    }
}
