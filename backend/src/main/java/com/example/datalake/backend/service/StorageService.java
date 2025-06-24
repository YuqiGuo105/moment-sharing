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
 * Assumes the supplied WebClient already has:
 *   • baseUrl = https://<project>.supabase.co/storage/v1
 *   • service-role headers (apikey + Authorization: Bearer …)
 */
@Service
public class StorageService {

    private final WebClient webClient;

    public StorageService(WebClient supabaseWebClient,
                          @Value("${supabase.storage.root}") String ignored) {
        this.webClient = supabaseWebClient;
    }

    /* ───────────── Buckets ───────────── */

    /** GET /storage/v1/bucket – list buckets */
    public Mono<String> listBuckets() {
        return webClient.get()
                .uri("/bucket")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.error(
                        new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to list buckets", e)));
    }

    /* ───────────── Downloads ───────────── */

    public Mono<ByteArrayResource> downloadFileByUrl(String fileUrl) {
        URI uri = URI.create(fileUrl);              // ← no more double-encoding
        return webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(ByteArrayResource.class)
                .onErrorResume(e -> Mono.error(
                        new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to download file", e)));
    }

    /* ───────────── Deletions ───────────── */

    /**
     * DELETE an object given any public / signed / direct URL.
     * Accepts:
     *   • …/storage/v1/object/{bucket}/{path}
     *   • …/storage/v1/object/public/{bucket}/{path}
     *   • …/storage/v1/object/sign/{bucket}/{path}?token=…
     */
    public Mono<String> deleteObjectByUrl(String fileUrl) {
        try {
            URI full = new URI(fileUrl);

            // 1) split only the *path* part
            String[] raw = full.getPath().split("/");
            List<String> seg = new ArrayList<>();
            for (String s : raw) if (!s.isEmpty()) seg.add(s);

            int objIdx = seg.indexOf("object");
            if (objIdx < 0 || objIdx + 1 >= seg.size()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "URL does not contain '/object/' segment"));
            }

            // 2) skip optional markers right after /object/
            int i = objIdx + 1;
            while (i < seg.size()
                    && ("public".equals(seg.get(i)) || "sign".equals(seg.get(i)))) {
                i++;
            }
            if (i >= seg.size()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "URL missing bucket name"));
            }

            String bucket = seg.get(i++);
            if (i >= seg.size()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "URL missing object path"));
            }

            // 3) join the remaining segments *exactly as they came* (already %-encoded)
            String objectPath = String.join("/", seg.subList(i, seg.size()));

            // 4) DELETE /storage/v1/object/{bucket}/{objectPath}
            URI endpoint = URI.create("/object/" + bucket + "/" + objectPath);

            return webClient.delete()
                    .uri(endpoint)                 // ← no re-encoding either
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.error(
                            new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Failed to delete object", e)));

        } catch (URISyntaxException ex) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file URL", ex));
        }
    }
}
