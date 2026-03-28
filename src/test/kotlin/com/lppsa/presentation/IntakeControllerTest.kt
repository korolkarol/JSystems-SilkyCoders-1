package com.lppsa.presentation

import com.lppsa.application.usecase.SubmitRequestUseCase
import com.lppsa.infrastructure.config.ActuatorSecurityConfig
import com.lppsa.presentation.web.IntakeController
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import kotlin.test.assertTrue

@WebFluxTest(IntakeController::class)
@Import(ActuatorSecurityConfig::class)
@TestPropertySource(
    properties = [
        "ACTUATOR_USER=test-user",
        "ACTUATOR_PASSWORD=test-password",
    ],
)
class IntakeControllerTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockitoBean
    lateinit var submitRequestUseCase: SubmitRequestUseCase

    @Test
    fun `GET slash returns 200 with text-html content type`() {
        webTestClient.get()
            .uri("/")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

    @Test
    fun `POST submit with valid multipart returns 200 text-event-stream`() {
        val fakeSessionId = "session-123"
        `when`(submitRequestUseCase.execute(any()))
            .thenReturn(fakeSessionId to flowOf("ACCEPT: Wniosek wstępnie pozytywny"))

        webTestClient.post()
            .uri("/submit")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(
                BodyInserters.fromMultipartData("requestType", "REKLAMACJA")
                    .with("productName", "Koszulka")
                    .with("purchaseDate", "2026-01-10")
                    .with("description", "Dziura w materiale")
                    .with("image", byteArrayOf(1, 2, 3))
                    .with("imageMimeType", "image/jpeg"),
            )
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
    }

    @Test
    fun `POST submit without image returns 400`() {
        webTestClient.post()
            .uri("/submit")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(
                BodyInserters.fromMultipartData("requestType", "REKLAMACJA")
                    .with("productName", "Koszulka")
                    .with("purchaseDate", "2026-01-10")
                    .with("description", "Opis"),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST submit with missing required field returns 400`() {
        webTestClient.post()
            .uri("/submit")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(
                BodyInserters.fromMultipartData("requestType", "REKLAMACJA")
                    .with("image", byteArrayOf(1, 2, 3)),
            )
            .exchange()
            .expectStatus().isBadRequest
    }
}
