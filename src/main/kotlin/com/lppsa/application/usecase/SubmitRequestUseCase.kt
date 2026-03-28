package com.lppsa.application.usecase

import com.lppsa.domain.model.Decision
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import com.lppsa.domain.port.EvaluationPort
import com.lppsa.domain.port.SessionRepository
import kotlinx.coroutines.flow.Flow
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
            val accumulated = StringBuilder()
            evaluationPort.evaluate(
                requestType = command.requestType,
                productName = command.productName,
                purchaseDate = command.purchaseDate,
                description = command.description,
                imageBytes = command.imageBytes,
                imageMimeType = command.imageMimeType,
            ).collect { token ->
                accumulated.append(token)
                emit(token)
            }
            val text = accumulated.toString()
            val decision = if (text.contains("ACCEPT") || text.contains("Wniosek wstępnie pozytywny")) {
                Decision.Accept(
                    explanation = text,
                    nextSteps = "",
                )
            } else {
                Decision.Reject(explanation = text)
            }
            sessionRepository.updateDecision(
                id = sessionId,
                decision = decision,
            )
        }
        return sessionId to streamFlow
    }
}
