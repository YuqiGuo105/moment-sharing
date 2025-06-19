package com.example.datalake.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Supabase Storage helper – URL-centric version.
 */
@Service
public class StorageService {
    private final WebClient webClient;

    /** Simple container for parsed Supabase URL pieces */
    private static class ParsedPath {
        final String bucket;
        final List<String> objectSegments;

        ParsedPath(String bucket, List<String> objectSegments) {
            this.bucket = bucket;
            this.objectSegments = objectSegments;
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

        // Use raw path so %2F etc. stay encoded. Remove duplicate slashes.
        String cleanPath = uri.getRawPath().replaceAll("/{2,}", "/");
        String[] rawSeg = cleanPath.split("/");

        List<String> seg = new ArrayList<>();
        for (String s : rawSeg) if (!s.isEmpty()) seg.add(s);

        int objIdx = seg.indexOf("object");
        if (objIdx == -1 || objIdx + 1 >= seg.size()) {
            throw new URISyntaxException(fileUrl, "URL does not contain expected '/object/' segment");
        }

        boolean hasPublic = objIdx + 1 < seg.size() && "public".equals(seg.get(objIdx + 1));
        String bucket = hasPublic ? seg.get(objIdx + 2) : seg.get(objIdx + 1);

        int pathStart = hasPublic ? objIdx + 3 : objIdx + 2;
        if (pathStart >= seg.size()) {
            throw new URISyntaxException(fileUrl, "URL missing object path");
        }

        List<String> objectSeg = new ArrayList<>();
        for (int i = pathStart; i < seg.size(); i++) {
            // decode each piece individually
            objectSeg.add(URLDecoder.decode(seg.get(i), StandardCharsets.UTF_8));
        }

        return new ParsedPath(bucket, objectSeg);
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
                            .uri(uriBuilder -> {
                                UriComponentsBuilder b = UriComponentsBuilder.fromPath("");
                                b.pathSegment("object", parsed.bucket);
                                b.pathSegment(parsed.objectSegments.toArray(new String[0]));
                                return b.build();
                            })
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
            ParsedPath parsed = parseUrl(fileUrl);

            return webClient.delete()
                    .uri(uriBuilder -> {
                        UriComponentsBuilder b = UriComponentsBuilder.fromPath("");
                        b.pathSegment("object", parsed.bucket);
                        b.pathSegment(parsed.objectSegments.toArray(new String[0]));
                        return b.build();
                    })
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
