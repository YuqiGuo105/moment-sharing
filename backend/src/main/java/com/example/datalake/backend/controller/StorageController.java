package com.example.datalake.backend.controller;

import com.example.datalake.backend.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Storage", description = "Supabase Storage operations")
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Operation(summary = "List all buckets", description = "Returns all available Supabase buckets")
    @GetMapping("/buckets")
    public Mono<String> listBuckets() {
        return storageService.listBuckets();
    }

    @Operation(summary = "Download file by URL", description = "Fetches a file from Supabase Storage using its URL")
    @GetMapping("/download")
    public Mono<?> download(@RequestParam String url) {
        return storageService.downloadFileByUrl(url);
    }

    @Operation(summary = "Upload file by URL", description = "Uploads a file to Supabase Storage given its URL")
    @PostMapping("/upload")
    public Mono<String> upload(@RequestParam String url) {
        return storageService.uploadFileByUrl(url);
    }

    @Operation(summary = "Delete file by URL", description = "Deletes a file from Supabase Storage given its URL")
    @DeleteMapping("/delete")
    public Mono<String> delete(@RequestParam String url) {
        return storageService.deleteObjectByUrl(url);
    }
}
