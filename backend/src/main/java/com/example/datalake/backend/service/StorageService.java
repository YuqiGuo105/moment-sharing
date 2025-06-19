package com.example.datalake.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Supabase Storage helper – URL-centric version.
 */
@Service
public class StorageService {
    private final WebClient webClient;

    public StorageService(WebClient supabaseWebClient,
                          @Value("${supabase.storage.root}") String ignored) {
        this.webClient = supabaseWebClient;          // baseUrl already set in config
    }

    /* ───────────── Buckets ───────────── */

    public Mono<String> listBuckets() {
        return webClient.get()
                .uri("/bucket")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.error(
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Failed to list buckets", e)));
    }

    /* ───────────── Downloads ───────────── */

    public Mono<ByteArrayResource> downloadFileByUrl(String fileUrl) {
        return webClient.get()
                .uri(fileUrl)                                    // absolute URL
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(ByteArrayResource.class)
                .onErrorResume(e -> Mono.error(
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download file", e)));
    }

    /* ───────────── Deletions ───────────── */

    public Mono<String> deleteObjectByUrl(String fileUrl) {
        URI uri;
        try {
            uri = new URI(fileUrl);
        } catch (URISyntaxException e) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file URL", e));
        }

        String cleanPath = uri.getPath().replaceAll("/{2,}", "/");
        String[] rawSeg  = cleanPath.split("/");

        // Remove empty segments from leading '/' or '://'
        List<String> seg = new ArrayList<>();
        for (String s : rawSeg) if (!s.isEmpty()) seg.add(s);

        // Expect path like ".../storage/v1/object/{public|sign}?/{bucket}/{objectPath}".
        int objIdx = seg.indexOf("object");
        if (objIdx == -1) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "URL does not contain '/object' segment"));
        }

        int bucketIdx = objIdx + 1;
        if (bucketIdx >= seg.size()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "URL missing bucket information"));
        }

        // Skip optional "public" or "sign" segments
        String first = seg.get(bucketIdx);
        if ("public".equals(first) || "sign".equals(first)) {
            bucketIdx++;
        }

        if (bucketIdx >= seg.size()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "URL missing bucket information"));
        }

        String bucket = seg.get(bucketIdx);
        int pathStart = bucketIdx + 1;
        if (pathStart >= seg.size()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "URL missing object path"));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = pathStart; i < seg.size(); i++) {
            if (sb.length() > 0) sb.append('/');
            sb.append(seg.get(i));
        }
        String objectPath = sb.toString();

        String endpoint = "/object/" + bucket + "/" + objectPath;

        return webClient.delete()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete object", e)));
    }
}
