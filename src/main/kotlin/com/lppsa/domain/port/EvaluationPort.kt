package com.lppsa.domain.port

import com.lppsa.domain.model.ChatMessage
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface EvaluationPort {
    fun evaluate(
        requestType: RequestType,
        productName: String,
        purchaseDate: String,
        description: String,
        imageBytes: ByteArray,
        imageMimeType: String,
    ): Flow<String>

    fun chat(
        session: Session,
        history: List<ChatMessage>,
        userMessage: String,
    ): Flow<String>
}
