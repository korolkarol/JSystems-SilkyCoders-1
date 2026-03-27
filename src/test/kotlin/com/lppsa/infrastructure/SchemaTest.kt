package com.lppsa.infrastructure

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.assertTrue

@SpringBootTest
class SchemaTest {
    @Autowired
    lateinit var jdbc: JdbcTemplate

    @Test
    fun `sessions table exists`() {
        val count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='sessions'",
            Int::class.java
        )
        assertTrue(count!! > 0, "sessions table must exist")
    }

    @Test
    fun `chat_messages table exists`() {
        val count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='chat_messages'",
            Int::class.java
        )
        assertTrue(count!! > 0, "chat_messages table must exist")
    }
}
