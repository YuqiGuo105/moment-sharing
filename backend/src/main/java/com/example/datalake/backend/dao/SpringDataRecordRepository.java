package com.example.datalake.backend.dao;

import com.example.datalake.backend.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data repository for Record entities.
 */
public interface SpringDataRecordRepository extends JpaRepository<Record, UUID> {
}
