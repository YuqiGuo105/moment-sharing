package com.example.datalake.backend.dao;

import com.example.datalake.backend.model.Record;
import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for Record entities.
 */
@Repository
public interface SpringDataRecordRepository extends FirestoreReactiveRepository<Record> {
}
