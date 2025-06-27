package com.example.datalake.backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class StorageService {

    /* ---------- client bootstrap (unchanged) ---------- */
    private final Storage storage;

    public StorageService(
            @Value("${firebase.credentials-file:}") String credentialsFile,
            @Value("${spring.cloud.gcp.project-id:}") String projectId) {

        try {
            StorageOptions.Builder b = StorageOptions.newBuilder();
            if (credentialsFile != null && !credentialsFile.isBlank()) {
                b.setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsFile))
                        .createScoped("https://www.googleapis.com/auth/cloud-platform"));
            } else {
                log.info("Using application-default credentials for GCS");
                b.setCredentials(GoogleCredentials.getApplicationDefault());
            }
            if (projectId != null && !projectId.isBlank()) b.setProjectId(projectId);
            this.storage = b.build().getService();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialise GCS client", ex);
        }
    }

    /* ---------- list ---------- */
    public Mono<String> listBuckets() {
        try {
            String result = StreamSupport.stream(storage.list().iterateAll().spliterator(), false)
                    .map(this::formatBucket)
                    .collect(Collectors.joining("\n"));
            return Mono.just(result);
        } catch (Exception e) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to list buckets", e));
        }
    }

    /* ---------- delete ---------- */
    public Mono<String> deleteObjectByUrl(String fileUrl) {
        try {
            Path p = parseGcsPath(fileUrl);
            boolean deleted = storage.delete(p.bucket(), p.object());

            if (!deleted) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
            }
            return Mono.just("Deleted");
        } catch (IllegalArgumentException iae) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, iae.getMessage(), iae));
        } catch (Exception ex) {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete failed", ex));
        }
    }

    /* ---------- helper: parse any Firebase / GCS URL to bucket + object ---------- */
    private static Path parseGcsPath(String url) {
        if (url.startsWith("gs://")) {                            // gs://bucket/path/file
            String path = url.substring(5);
            int slash = path.indexOf('/');
            if (slash < 0) throw new IllegalArgumentException("Missing object path");
            return new Path(path.substring(0, slash), path.substring(slash + 1));
        }

        URI uri = URI.create(url);
        String host = uri.getHost();
        if (host == null) throw new IllegalArgumentException("Invalid URL");

        /* https://<bucket>.storage.googleapis.com/<object> */
        if (host.endsWith(".storage.googleapis.com")) {
            String bucket = host.substring(0, host.length() - ".storage.googleapis.com".length());
            String object = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
            return new Path(bucket, URLDecoder.decode(object, StandardCharsets.UTF_8));
        }

        /* https://firebasestorage.googleapis.com/v0/b/<bucket>/o/<object> */
        if (host.endsWith("firebasestorage.googleapis.com")) {
            String[] seg = uri.getPath().split("/");
            int bIdx = Arrays.asList(seg).indexOf("b");
            int oIdx = Arrays.asList(seg).indexOf("o");
            if (bIdx < 0 || oIdx < 0 || bIdx + 1 >= seg.length || oIdx + 1 >= seg.length) {
                throw new IllegalArgumentException("Malformed Firebase Storage URL");
            }
            String bucket = seg[bIdx + 1];
            String object = String.join("/", Arrays.copyOfRange(seg, oIdx + 1, seg.length));
            return new Path(bucket, URLDecoder.decode(object, StandardCharsets.UTF_8));
        }

        throw new IllegalArgumentException("Unsupported GCS URL host: " + host);
    }

    /* ---------- helper: pretty-print ---------- */
    private String formatBucket(Bucket b) {
        String files = StreamSupport.stream(b.list().iterateAll().spliterator(), false)
                .map(Blob::getName)
                .collect(Collectors.joining("\n  ", "  ", ""));
        return b.getName() + ":\n" + files;
    }

    /* ---------- tiny value object (Java 17 record) ---------- */
    private record Path(String bucket, String object) { }
}
