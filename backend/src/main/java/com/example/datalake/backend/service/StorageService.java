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
 *
 * Assumes the supplied WebClient already has:
 *   • baseUrl = https://<project>.supabase.co/storage/v1
 *   • default headers: apikey + Authorization: Bearer <service_role_key>
 */
@Service
public class StorageService {

    private final WebClient webClient;

    public StorageService(WebClient supabaseWebClient,
                          @Value("${supabase.storage.root}") String ignored) {
        this.webClient = supabaseWebClient;   // baseUrl is pre-configured
    }

    /* ───────────── Buckets ───────────── */

    /** GET /storage/v1/bucket – returns JSON array of bucket objects */
    public Mono<String> listBuckets() {
        return webClient.get()
                .uri("/bucket")                    // full path = /storage/v1/bucket
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.error(
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to list buckets", e)));
    }

    /* ───────────── Downloads ───────────── */

    public Mono<ByteArrayResource> downloadFileByUrl(String fileUrl) {
        return webClient.get()
                .uri(fileUrl)                      // absolute URL
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(ByteArrayResource.class)
                .onErrorResume(e -> Mono.error(
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to download file", e)));
    }

    /* ───────────── Deletions ───────────── */

    /**
     * DELETE an object given its public / signed / direct URL.
     *
     * Accepts all of the following patterns:
     *   • …/storage/v1/object/{bucket}/{path…}
     *   • …/storage/v1/object/public/{bucket}/{path…}
     *   • …/storage/v1/object/sign/{bucket}/{path…}?token=…
     */
    public Mono<String> deleteObjectByUrl(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);

            // 1) Split path, drop empty segments
            String[] rawSegments = uri.getPath().split("/");
            List<String> seg = new ArrayList<>();
            for (String s : rawSegments) if (!s.isEmpty()) seg.add(s);

            int objIdx = seg.indexOf("object");
            if (objIdx == -1 || objIdx + 1 >= seg.size()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "URL does not contain expected '/object/' segment"));
            }

            // 2) Skip optional markers that may appear right after /object/
            int cursor = objIdx + 1;
            while (cursor < seg.size()
                    && ("public".equals(seg.get(cursor))
                    || "sign".equals(seg.get(cursor)))) {
                cursor++;
            }
            if (cursor >= seg.size()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "URL missing bucket name"));
            }

            String bucket = seg.get(cursor++);
            if (cursor >= seg.size()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "URL missing object path"));
            }

            // 3) Re-assemble remaining segments for object path
            StringBuilder sb = new StringBuilder();
            for (int i = cursor; i < seg.size(); i++) {
                if (sb.length() > 0) sb.append('/');
                sb.append(seg.get(i));
            }
            String objectPath = sb.toString();                 // no leading '/'

            // 4) DELETE /storage/v1/object/{bucket}/{objectPath}
            String endpoint = "/object/" + bucket + "/" + objectPath;

            return webClient.delete()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.error(
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Failed to delete object", e)));

        } catch (URISyntaxException ex) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file URL", ex));
        }
    }
}
