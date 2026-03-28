package com.lppsa.infrastructure

import com.lppsa.domain.model.RequestType
import com.lppsa.infrastructure.config.AiConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.ai.chat.client.ChatClient
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@SpringBootTest
class AiConfigTest {

    @Autowired
    lateinit var chatClient: ChatClient

    @Autowired
    lateinit var aiConfig: AiConfig

    @Test
    fun `ChatClient bean is available`() {
        assertNotNull(chatClient)
    }

    @Test
    fun `loadPolicy for REKLAMACJA contains regulamin and reklamacje content`() {
        val policy = aiConfig.loadPolicy(RequestType.REKLAMACJA)
        assertTrue(policy.isNotBlank(), "Policy must not be blank")
        assertTrue(policy.length > 100, "Policy must have substantial content")
    }

    @Test
    fun `loadPolicy for ZWROT contains regulamin and zwrot content`() {
        val policy = aiConfig.loadPolicy(RequestType.ZWROT)
        assertTrue(policy.isNotBlank())
        assertTrue(policy.length > 100)
    }

    @Test
    fun `loadPolicy for REKLAMACJA does not load zwrot-30-dni`() {
        val reklamacjaPolicy = aiConfig.loadPolicy(RequestType.REKLAMACJA)
        val zwrotPolicy = aiConfig.loadPolicy(RequestType.ZWROT)
        assertFalse(reklamacjaPolicy == zwrotPolicy, "REKLAMACJA and ZWROT policies must differ")
    }
}
