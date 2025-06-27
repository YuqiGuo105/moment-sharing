package com.example.datalake.backend.controller;

import com.example.datalake.backend.dto.RecordDto;
import com.example.datalake.backend.dao.SpringDataRecordRepository;
import com.example.datalake.backend.model.Record;
import com.example.datalake.backend.service.RecordService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RecordControllerTest {

    @Test
    void createAndGet() {

    }
}
