package com.example.datalake.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Supabase Storage helper – URL-centric version.
 */
@Service
public class StorageService {
    private final WebClient webClient;

    /** Simple container for parsed Supabase URL pieces */
    private static class ParsedPath {
        final String bucket;
        final String objectPath; // already URL encoded, may contain slashes

        ParsedPath(String bucket, String objectPath) {
            this.bucket = bucket;
            this.objectPath = objectPath;
        }
    }

    public StorageService(WebClient supabaseWebClient,
                          @Value("${supabase.storage.root}") String ignored) {
        this.webClient = supabaseWebClient;          // baseUrl already set in config
    }

    /**
     * Parse a Supabase Storage URL and extract bucket and path segments.
     * This method is lenient with "public" URLs and normalises duplicate slashes.
     */
    private ParsedPath parseUrl(String fileUrl) throws URISyntaxException {
        URI uri = new URI(fileUrl);

        // Keep raw path so we do not double decode
        String rawPath = uri.getRawPath();
        int idx = rawPath.indexOf("/object/");
        if (idx == -1) {
            throw new URISyntaxException(fileUrl, "URL missing '/object/' segment");
        }

        String remainder = rawPath.substring(idx + 8); // after '/object/'
        if (remainder.startsWith("public/")) {
            remainder = remainder.substring(7); // strip 'public/'
        }

        int slash = remainder.indexOf('/');
        if (slash == -1) {
            throw new URISyntaxException(fileUrl, "URL missing bucket or object path");
        }

        String bucket = remainder.substring(0, slash);
        String objectPath = remainder.substring(slash + 1); // keep encoded
        if (objectPath.isEmpty()) {
            throw new URISyntaxException(fileUrl, "URL missing object path");
        }

        return new ParsedPath(bucket, objectPath);
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
            ParsedPath parsed = parseUrl(fileUrl);

            /* 3) GET the remote file */
            Mono<byte[]> bytesMono = webClient.get()
                    .uri(fileUrl)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono(byte[].class);

            /* 4) POST /object/{bucket}/{objectPath} */
            return bytesMono.flatMap(bytes ->
                    webClient.post()
                            .uri("/object/" + parsed.bucket + "/" + parsed.objectPath)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .bodyValue(bytes)
                            .retrieve()
                            .bodyToMono(String.class))
                    .onErrorResume(WebClientResponseException.class,
                            ex -> Mono.error(new ResponseStatusException(
                                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex)))
                    .onErrorResume(e -> Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e)));

        } catch (URISyntaxException ex) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file URL", ex));
        }
    }

    /* ───────────── Deletions ───────────── */

    public Mono<String> deleteObjectByUrl(String fileUrl) {
        try {
            ParsedPath parsed = parseUrl(fileUrl);

            return webClient.delete()
                    .uri("/object/" + parsed.bucket + "/" + parsed.objectPath)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class,
                            ex -> Mono.error(new ResponseStatusException(
                                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex)))
                    .onErrorResume(e -> Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete object", e)));

        } catch (URISyntaxException ex) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file URL", ex));
        }
    }
}
