package com.example.datalake.backend.controller;

import com.example.datalake.backend.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST endpoints exposing {@link StorageService} operations.
 */
@RestController
@RequestMapping("/storage")
@Tag(name = "Storage", description = "Supabase storage operations")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * List all buckets available in Supabase.
     */
    @GetMapping("/buckets")
    @Operation(summary = "List buckets")
    public Mono<String> listBuckets() {
        return storageService.listBuckets();
    }

    /**
     * Download a file using its public URL.
     *
     * @param url absolute URL pointing to a Supabase storage object
     */
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Download object by URL")
    public Mono<ByteArrayResource> download(@Parameter(description = "Object URL")
                                            @RequestParam("url") String url) {
        return storageService.downloadFileByUrl(url);
    }

    /**
     * Delete an object given its public or signed URL.
     *
     * @param url absolute URL pointing to a Supabase storage object
     */
    @DeleteMapping("/object")
    @Operation(summary = "Delete object by URL")
    public Mono<String> delete(@Parameter(description = "Object URL")
                               @RequestParam("url") String url) {
        return storageService.deleteObjectByUrl(url);
    }
}
