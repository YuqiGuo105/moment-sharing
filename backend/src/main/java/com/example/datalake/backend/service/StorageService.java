package com.example.datalake.backend.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class StorageService {

    private final Storage storage;

    public StorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    /**
     * Return a textual listing of all buckets and their file paths.
     */
    public Mono<String> listBuckets() {
        try {
            String result = StreamSupport.stream(storage.list().iterateAll().spliterator(), false)
                    .map(bucket -> formatBucket(bucket))
                    .collect(Collectors.joining("\n"));
            return Mono.just(result);
        } catch (Exception e) {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to list buckets", e));
        }
    }

    private String formatBucket(Bucket bucket) {
        String files = StreamSupport.stream(bucket.list().iterateAll().spliterator(), false)
                .map(Blob::getName)
                .collect(Collectors.joining("\n  ", "  ", ""));
        return bucket.getName() + ":\n" + files;
    }

    /**
     * Delete a file using either a gs:// or https:// URL.
     */
    public Mono<String> deleteObjectByUrl(String fileUrl) {
        try {
            String bucket;
            String object;
            if (fileUrl.startsWith("gs://")) {
                String withoutScheme = fileUrl.substring(5);
                int slash = withoutScheme.indexOf('/');
                if (slash < 0) throw new IllegalArgumentException("Missing object path");
                bucket = withoutScheme.substring(0, slash);
                object = withoutScheme.substring(slash + 1);
            } else {
                URI uri = URI.create(fileUrl);
                String host = uri.getHost();
                if (host == null || !host.endsWith("storage.googleapis.com")) {
                    throw new IllegalArgumentException("Unsupported URL");
                }
                String[] parts = uri.getPath().split("/", 3);
                if (parts.length < 3) throw new IllegalArgumentException("Missing object path");
                bucket = parts[1];
                object = parts[2];
            }

            boolean deleted = storage.delete(bucket, object);
            if (!deleted) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
            }
            return Mono.just("Deleted");
        } catch (Exception ex) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file URL", ex));
        }
    }
}
