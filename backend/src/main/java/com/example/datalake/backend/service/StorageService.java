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

    /* ───────────── Uploads ───────────── */

    public Mono<String> uploadFileByUrl(String fileUrl) {
        try {
            /* 1) Normalise the path */
            URI uri = new URI(fileUrl);
            String cleanPath = uri.getPath().replaceAll("/{2,}", "/");
            String[] rawSeg = cleanPath.split("/");

            /* 2) Strip empty segments caused by leading "/" or "://" */
            List<String> seg = new ArrayList<>();
            for (String s : rawSeg) if (!s.isEmpty()) seg.add(s);

            /* Expect “…/storage/v1/object/(public/)?{bucket}/{objectPath…}” */
            int objIdx = seg.indexOf("object");
            if (objIdx == -1 || objIdx + 1 >= seg.size()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "URL does not contain expected '/object/' segment"));
            }

            boolean hasPublic = "public".equals(seg.get(objIdx + 1));
            String bucket = hasPublic ? seg.get(objIdx + 2)
                    : seg.get(objIdx + 1);

            int pathStart = hasPublic ? objIdx + 3
                    : objIdx + 2;
            if (pathStart >= seg.size()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "URL missing object path"));
            }

            /* Join remaining segments to reconstruct the object path */
            StringBuilder sb = new StringBuilder();
            for (int i = pathStart; i < seg.size(); i++) {
                if (sb.length() > 0) sb.append('/');
                sb.append(seg.get(i));
            }
            String objectPath = sb.toString();

            /* 3) GET the remote file */
            Mono<byte[]> bytesMono = webClient.get()
                    .uri(fileUrl)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono(byte[].class);

            /* 4) POST /object/{bucket}/{objectPath} */
            String endpoint = "/object/" + bucket + "/" + objectPath;
            return bytesMono.flatMap(bytes ->
                    webClient.post()
                            .uri(endpoint)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .bodyValue(bytes)
                            .retrieve()
                            .bodyToMono(String.class))
                    .onErrorResume(e -> Mono.error(
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e)));

        } catch (URISyntaxException ex) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file URL", ex));
        }
    }

    /* ───────────── Deletions ───────────── */

    public Mono<String> deleteObjectByUrl(String fileUrl) {
        try {
            /* 1) Normalise the path */
            URI uri          = new URI(fileUrl);
            String cleanPath = uri.getPath().replaceAll("/{2,}", "/");   // collapse "//"
            String[] rawSeg  = cleanPath.split("/");

            /* 2) Strip empty segments caused by leading "/" or "://" */
            List<String> seg = new ArrayList<>();
            for (String s : rawSeg) if (!s.isEmpty()) seg.add(s);

            /* Expect “…/storage/v1/object/(public/)?{bucket}/{objectPath…}” */
            int objIdx = seg.indexOf("object");
            if (objIdx == -1 || objIdx + 1 >= seg.size()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "URL does not contain expected '/object/' segment"));
            }

            boolean hasPublic = "public".equals(seg.get(objIdx + 1));
            String bucket     = hasPublic ? seg.get(objIdx + 2)
                    : seg.get(objIdx + 1);

            int pathStart     = hasPublic ? objIdx + 3
                    : objIdx + 2;
            if (pathStart >= seg.size()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "URL missing object path"));
            }

            /* Join remaining segments to reconstruct the object path */
            StringBuilder sb = new StringBuilder();
            for (int i = pathStart; i < seg.size(); i++) {
                if (sb.length() > 0) sb.append('/');
                sb.append(seg.get(i));
            }
            String objectPath = sb.toString();     // no leading "/"

            /* 3) DELETE /object/{bucket}/{objectPath} */
            String endpoint = "/object/" + bucket + "/" + objectPath;

            return webClient.delete()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.error(
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete object", e)));

        } catch (URISyntaxException ex) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file URL", ex));
        }
    }
}
