package com.example.datalake.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class BackendApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void pingEndpointReturnsPong() {
        webTestClient.get().uri("/api/ping").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("pong");
    }
}
