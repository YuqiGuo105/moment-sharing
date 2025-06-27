package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.model.Record;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordCleanupScheduler {

    private final SpringDataRecordRepository repo;
    private final StorageService storageService;

    private static final Duration TTL = Duration.ofHours(24);

    @Scheduled(fixedDelayString = "${record.cleanup.interval:3600000}")
    public void removeExpiredRecords() {
        Instant cutoff = Instant.now().minus(TTL);
        repo.findAll()
                .filter(record -> isExpired(record, cutoff))
                .flatMap(this::deleteRecord)
                .subscribe(
                        r -> log.info("Removed expired record {}", r.getId()),
                        ex -> log.error("Error during record cleanup", ex)
                );
    }

    private boolean isExpired(Record r, Instant cutoff) {
        if (r.getCreatedAt() == null) return false;
        Instant created = r.getCreatedAt().toDate().toInstant();
        return created.isBefore(cutoff);
    }

    private Mono<Record> deleteRecord(Record r) {
        return storageService.deleteObjectByUrl(r.getUrl())
                .onErrorResume(e -> {
                    log.warn("Failed to delete file {}", r.getUrl(), e);
                    return Mono.empty();
                })
                .then(repo.deleteById(r.getId()))
                .thenReturn(r);
    }
}
