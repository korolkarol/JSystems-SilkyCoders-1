package com.lppsa.presentation.web

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(-2)
class GlobalErrorHandler : ErrorWebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        // Let Spring handle ResponseStatusException (4xx) normally
        if (ex is org.springframework.web.server.ResponseStatusException) {
            return Mono.error(ex)
        }

        val response = exchange.response
        if (response.isCommitted) {
            return Mono.error(ex)
        }

        response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
        response.headers.contentType = MediaType.TEXT_HTML

        val message = "Wystąpił nieoczekiwany błąd. Spróbuj ponownie lub skontaktuj się z obsługą klienta."
        val html = """<p class="error-message">$message</p>"""
        val buffer = response.bufferFactory().wrap(html.toByteArray(Charsets.UTF_8))
        return response.writeWith(Mono.just(buffer))
    }
}
