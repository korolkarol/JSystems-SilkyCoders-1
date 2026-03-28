package com.lppsa.presentation.web

import com.lppsa.application.usecase.SendChatMessageUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange

@RestController
class ChatController(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
) {

    @PostMapping(
        "/chat/{sessionId}",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    suspend fun chat(
        @PathVariable sessionId: String,
        exchange: ServerWebExchange,
    ): Flow<String> {
        val formData = exchange.formData.awaitSingle()
        val message = formData.getFirst("message").orEmpty()
        if (message.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Wiadomość nie może być pusta.")
        }

        return sendChatMessageUseCase.execute(
            sessionId = sessionId,
            userMessage = message,
        ).catch { e ->
            when (e) {
                is NoSuchElementException -> throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Sesja nie została znaleziona.",
                )
                else -> throw e
            }
        }
    }
}
