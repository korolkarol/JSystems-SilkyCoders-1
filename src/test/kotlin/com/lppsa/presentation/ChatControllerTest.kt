package com.lppsa.presentation

import com.lppsa.application.usecase.SendChatMessageUseCase
import com.lppsa.infrastructure.config.ActuatorSecurityConfig
import com.lppsa.presentation.web.ChatController
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@WebFluxTest(ChatController::class)
@Import(ActuatorSecurityConfig::class)
@TestPropertySource(
    properties = [
        "ACTUATOR_USER=test-user",
        "ACTUATOR_PASSWORD=test-password",
    ],
)
class ChatControllerTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockitoBean
    lateinit var sendChatMessageUseCase: SendChatMessageUseCase

    @Test
    fun `POST chat returns SSE stream`() {
        `when`(sendChatMessageUseCase.execute(any(), any()))
            .thenReturn(flowOf("Odpowiedź asystenta"))

        webTestClient.post()
            .uri("/chat/session-123")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("message", "Pytanie"))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
    }

    @Test
    fun `POST chat with unknown sessionId returns 404 with Polish message`() {
        `when`(sendChatMessageUseCase.execute(eq("non-existent"), any()))
            .thenReturn(
                kotlinx.coroutines.flow.flow {
                    throw NoSuchElementException("Session not found: non-existent")
                },
            )

        webTestClient.post()
            .uri("/chat/non-existent")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("message", "Pytanie"))
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `POST chat with blank message returns 400`() {
        webTestClient.post()
            .uri("/chat/session-123")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("message", "   "))
            .exchange()
            .expectStatus().isBadRequest
    }
}
