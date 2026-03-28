package com.lppsa.infrastructure.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    locations = ["classpath:application-test.properties"],
    properties = [
        "ACTUATOR_USER=test-user",
        "ACTUATOR_PASSWORD=test-password",
    ],
)
class ActuatorSecurityConfigTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `health endpoint is publicly accessible`() {
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `prometheus endpoint returns 401 without credentials`() {
        webTestClient.get()
            .uri("/actuator/prometheus")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `prometheus endpoint returns 200 with valid ACTUATOR role credentials`() {
        webTestClient.get()
            .uri("/actuator/prometheus")
            .headers { it.setBasicAuth("test-user", "test-password") }
            .exchange()
            .expectStatus().isOk
    }
}
