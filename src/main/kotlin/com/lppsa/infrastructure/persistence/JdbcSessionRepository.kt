package com.lppsa.infrastructure.persistence

import com.lppsa.domain.model.Decision
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import com.lppsa.domain.port.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class JdbcSessionRepository(private val jdbc: JdbcTemplate) : SessionRepository {

    override suspend fun save(session: Session): Unit = withContext(Dispatchers.IO) {
        jdbc.update(
            """
            INSERT INTO sessions (id, request_type, product_name, purchase_date, description, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            session.id,
            session.requestType.name,
            session.productName,
            session.purchaseDate,
            session.description,
            session.createdAt,
        )
    }

    override suspend fun findById(id: String): Session? = withContext(Dispatchers.IO) {
        jdbc.query(
            "SELECT * FROM sessions WHERE id = ?",
            { rs, _ ->
                val outcomeStr = rs.getString("decision_outcome")
                val explanation = rs.getString("decision_explanation")
                val mismatch = rs.getString("mismatch_recommendation")
                val decision = when (outcomeStr) {
                    "ACCEPT" -> Decision.Accept(
                        explanation = explanation ?: "",
                        nextSteps = mismatch ?: "",
                    )
                    "REJECT" -> Decision.Reject(
                        explanation = explanation ?: "",
                        mismatchRecommendation = mismatch,
                    )
                    else -> null
                }
                Session(
                    id = rs.getString("id"),
                    requestType = RequestType.valueOf(rs.getString("request_type")),
                    productName = rs.getString("product_name"),
                    purchaseDate = rs.getString("purchase_date"),
                    description = rs.getString("description"),
                    decision = decision,
                    createdAt = rs.getString("created_at"),
                )
            },
            id,
        ).firstOrNull()
    }

    override suspend fun updateDecision(id: String, decision: Decision): Unit = withContext(Dispatchers.IO) {
        when (decision) {
            is Decision.Accept -> jdbc.update(
                "UPDATE sessions SET decision_outcome = 'ACCEPT', decision_explanation = ?, mismatch_recommendation = ? WHERE id = ?",
                decision.explanation,
                decision.nextSteps,
                id,
            )
            is Decision.Reject -> jdbc.update(
                "UPDATE sessions SET decision_outcome = 'REJECT', decision_explanation = ?, mismatch_recommendation = ? WHERE id = ?",
                decision.explanation,
                decision.mismatchRecommendation,
                id,
            )
        }
    }
}
