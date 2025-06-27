package com.example.datalake.backend.controller;

import com.example.datalake.backend.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storage")
@Tag(name = "Storage", description = "Operations for Supabase Storage")
public class StorageController {

    private final StorageService service;

    public StorageController(StorageService service) {
        this.service = service;
    }

    @GetMapping("/buckets")
    @Operation(summary = "List all storage buckets")
    public ResponseEntity<String> listBuckets() {
        String body = service.listBuckets().block();
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Download a file using its URL")
    public ResponseEntity<ByteArrayResource> download(@RequestParam("url") String fileUrl) {
        ByteArrayResource resource = service.downloadFileByUrl(fileUrl).block();
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/object")
    @Operation(summary = "Delete an object using its URL")
    public ResponseEntity<String> delete(@RequestParam("url") String fileUrl) {
        String body = service.deleteObjectByUrl(fileUrl).block();
        return ResponseEntity.ok(body);
    }
}
