package com.example.datalake.backend.record;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/records.sql")
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listReturnsJsonArray() throws Exception {
        mockMvc.perform(get("/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getReturnsSingleRecord() throws Exception {
        mockMvc.perform(get("/records/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("alice"));
    }

    @Test
    void createPersistsAndReturnsDto() throws Exception {
        String body = "{\"url\":\"http://example.com/3\",\"owner\":\"carol\"}";
        mockMvc.perform(post("/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner").value("carol"));

        mockMvc.perform(get("/records"))
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void updateModifiesExistingRecord() throws Exception {
        String body = "{\"url\":\"http://example.com/new\",\"owner\":\"bob\"}";
        mockMvc.perform(put("/records/11111111-1111-1111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://example.com/new"));
    }

    @Test
    void deleteRemovesRecord() throws Exception {
        mockMvc.perform(delete("/records/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/records"))
                .andExpect(jsonPath("$.length()").value(1));
    }
}
