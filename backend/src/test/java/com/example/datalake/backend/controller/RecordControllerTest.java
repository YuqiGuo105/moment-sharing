package com.example.datalake.backend.controller;

import com.example.datalake.backend.dto.RecordDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecordControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createAndListRecord() throws Exception {
        RecordDto dto = new RecordDto();
        dto.setUrl("http://example.com/test.jpg");
        dto.setOwner("alice");

        MvcResult result = mvc.perform(post("/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        RecordDto response = mapper.readValue(result.getResponse().getContentAsString(), RecordDto.class);
        assertEquals(dto.getUrl(), response.getUrl());
        assertEquals(dto.getOwner(), response.getOwner());
        assertNotNull(response.getCreatedAt());

        mvc.perform(get("/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
