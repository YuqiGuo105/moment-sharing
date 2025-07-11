package com.example.datalake.backend.dao;

import com.example.datalake.backend.model.Record;
import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for Record entities.
 */
@Repository
public interface SpringDataRecordRepository extends FirestoreReactiveRepository<Record> {

    /**
     * Find all records created before the given timestamp.
     *
     * @param timestamp cutoff timestamp
     * @return flux of matching records
     */
    reactor.core.publisher.Flux<Record> findByCreatedAtLessThan(com.google.cloud.Timestamp timestamp);

    /**
     * Find all records owned by the given username.
     *
     * @param owner record owner
     * @return flux of matching records
     */
    reactor.core.publisher.Flux<Record> findByOwner(String owner);
}
