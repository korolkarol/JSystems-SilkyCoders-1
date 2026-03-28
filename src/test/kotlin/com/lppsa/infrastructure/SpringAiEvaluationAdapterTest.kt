package com.lppsa.infrastructure

import com.lppsa.domain.model.RequestType
import com.lppsa.domain.port.EvaluationPort
import com.lppsa.infrastructure.ai.SpringAiEvaluationAdapter
import com.lppsa.infrastructure.config.AiConfig
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.ai.chat.client.ChatClient
import kotlin.test.assertTrue

class SpringAiEvaluationAdapterTest {

    @Test
    fun `loadPolicy returns non-blank content for REKLAMACJA`() {
        val aiConfig = AiConfig()
        val policy = aiConfig.loadPolicy(RequestType.REKLAMACJA)
        assertTrue(policy.isNotBlank())
        assertTrue(policy.length > 100)
    }

    @Test
    fun `loadPolicy returns non-blank content for ZWROT`() {
        val aiConfig = AiConfig()
        val policy = aiConfig.loadPolicy(RequestType.ZWROT)
        assertTrue(policy.isNotBlank())
        assertTrue(policy.length > 100)
    }

    @Test
    fun `loadPolicy differs between REKLAMACJA and ZWROT`() {
        val aiConfig = AiConfig()
        val reklamacjaPolicy = aiConfig.loadPolicy(RequestType.REKLAMACJA)
        val zwrotPolicy = aiConfig.loadPolicy(RequestType.ZWROT)
        assertTrue(reklamacjaPolicy != zwrotPolicy, "Policies must differ by request type")
    }

    @Test
    fun `SpringAiEvaluationAdapter implements EvaluationPort`() {
        val mockChatClient = mock(ChatClient::class.java)
        val aiConfig = AiConfig()
        val adapter = SpringAiEvaluationAdapter(
            chatClient = mockChatClient,
            aiConfig = aiConfig,
        )
        assertTrue(adapter is EvaluationPort)
    }
}
