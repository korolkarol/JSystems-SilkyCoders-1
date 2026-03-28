package com.lppsa.application.usecase

import com.lppsa.domain.model.Decision
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import com.lppsa.domain.port.EvaluationPort
import com.lppsa.domain.port.SessionRepository
import com.lppsa.infrastructure.metrics.BusinessMetrics
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

data class SubmitCommand(
    val requestType: RequestType,
    val productName: String,
    val purchaseDate: String,
    val description: String,
    val imageBytes: ByteArray,
    val imageMimeType: String,
)

@Service
class SubmitRequestUseCase(
    private val sessionRepository: SessionRepository,
    private val evaluationPort: EvaluationPort,
    private val businessMetrics: BusinessMetrics,
) {
    fun execute(command: SubmitCommand): Pair<String, Flow<String>> {
        val sessionId = UUID.randomUUID().toString()
        val streamFlow = flow {
            val session = Session(
                id = sessionId,
                requestType = command.requestType,
                productName = command.productName,
                purchaseDate = command.purchaseDate,
                description = command.description,
                decision = null,
                createdAt = LocalDateTime.now().toString(),
            )
            sessionRepository.save(session)
            businessMetrics.recordSessionSubmitted(requestType = command.requestType.name)
            val accumulated = StringBuilder()
            val timer = businessMetrics.evaluationTimer(requestType = command.requestType.name)
            val sample = Timer.start()
            evaluationPort.evaluate(
                requestType = command.requestType,
                productName = command.productName,
                purchaseDate = command.purchaseDate,
                description = command.description,
                imageBytes = command.imageBytes,
                imageMimeType = command.imageMimeType,
            ).catch { cause ->
                sample.stop(timer)
                emit("ERROR:${friendlyErrorMessage(cause)}")
            }.collect { token ->
                accumulated.append(token)
                emit(token)
            }
            sample.stop(timer)
            val text = accumulated.toString()
            val decision = if (text.contains("ACCEPT") || text.contains("Wniosek wstępnie pozytywny")) {
                Decision.Accept(
                    explanation = text,
                    nextSteps = "",
                )
            } else {
                Decision.Reject(explanation = text)
            }
            businessMetrics.recordDecision(
                requestType = command.requestType.name,
                outcome = if (decision is Decision.Accept) { "accept" } else { "reject" },
            )
            sessionRepository.updateDecision(
                id = sessionId,
                decision = decision,
            )
        }
        return sessionId to streamFlow
    }

    private fun friendlyErrorMessage(cause: Throwable): String = when (cause) {
        is org.springframework.web.reactive.function.client.WebClientResponseException ->
            "Usługa AI jest chwilowo niedostępna (${cause.statusCode.value()}). Spróbuj ponownie za chwilę."
        else -> "Wystąpił błąd podczas przetwarzania zgłoszenia. Spróbuj ponownie."
    }
}
