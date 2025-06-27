package com.example.datalake.backend.controller;

import com.example.datalake.backend.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storage")
@Tag(name = "Storage", description = "Operations for Firebase Storage")
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


    @DeleteMapping("/object")
    @Operation(summary = "Delete an object using its URL")
    public ResponseEntity<String> delete(@RequestParam("url") String fileUrl) {
        String body = service.deleteObjectByUrl(fileUrl).block();
        return ResponseEntity.ok(body);
    }
}
