package com.example.datalake.backend.service;

import com.example.datalake.backend.model.Record;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple file-based implementation of RecordService used for tests.
 * Data is stored as JSON in the provided file path.
 */
public class FileRecordService extends RecordService {
    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<UUID, Record> data;

    public FileRecordService(Path file) throws IOException {
        super((com.google.cloud.firestore.Firestore) null);
        this.file = file;
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                List<Record> list = mapper.readValue(r, new TypeReference<List<Record>>(){});
                data = list.stream().collect(Collectors.toMap(Record::getId, v -> v));
            }
        } else {
            data = new HashMap<>();
        }
    }

    private void persist() throws IOException {
        try (Writer w = Files.newBufferedWriter(file)) {
            mapper.writeValue(w, data.values());
        }
    }

    @Override
    public List<Record> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Record findById(UUID id) {
        return data.get(id);
    }

    @Override
    public Record save(Record record) {
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(OffsetDateTime.now());
        }
        data.put(record.getId(), record);
        try {
            persist();
        } catch (IOException ignored) {
        }
        return record;
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
        try {
            persist();
        } catch (IOException ignored) {
        }
    }
}
