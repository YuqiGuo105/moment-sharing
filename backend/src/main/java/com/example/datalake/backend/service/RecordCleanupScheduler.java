package com.example.datalake.backend.service;

import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.model.Record;
import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${record.cleanup.ttl-hours:24}")
    private long ttlHours;

    @Scheduled(fixedDelayString = "${record.cleanup.interval:3600000}")
    public void removeExpiredRecords() {
        Duration ttl = Duration.ofHours(ttlHours);
        Instant cutoffInstant = Instant.now().minus(ttl);
        Timestamp cutoff = Timestamp.ofTimeSecondsAndNanos(
                cutoffInstant.getEpochSecond(), cutoffInstant.getNano());

        repo.findByCreatedAtLessThan(cutoff)
                .flatMap(this::deleteRecord)
                .subscribe(
                        r -> log.info("Removed expired record {}", r.getId()),
                        ex -> log.error("Error during record cleanup", ex)
                );
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
