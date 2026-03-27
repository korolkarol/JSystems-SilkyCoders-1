package com.lppsa.domain.port

import com.lppsa.domain.model.Decision
import com.lppsa.domain.model.Session

interface SessionRepository {
    suspend fun save(session: Session)
    suspend fun findById(id: String): Session?
    suspend fun updateDecision(id: String, decision: Decision)
}
