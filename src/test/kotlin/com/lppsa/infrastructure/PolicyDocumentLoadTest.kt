package com.lppsa.infrastructure

import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import kotlin.test.assertTrue

class PolicyDocumentLoadTest {

    @Test
    fun `regulamin md loads from classpath`() {
        val resource = ClassPathResource("policy/regulamin.md")
        assertTrue(resource.exists(), "policy/regulamin.md must exist on classpath")
        assertTrue(resource.contentLength() > 0, "policy/regulamin.md must not be empty")
    }

    @Test
    fun `reklamacje md loads from classpath`() {
        val resource = ClassPathResource("policy/reklamacje.md")
        assertTrue(resource.exists(), "policy/reklamacje.md must exist on classpath")
        assertTrue(resource.contentLength() > 0, "policy/reklamacje.md must not be empty")
    }

    @Test
    fun `zwrot-30-dni md loads from classpath`() {
        val resource = ClassPathResource("policy/zwrot-30-dni.md")
        assertTrue(resource.exists(), "policy/zwrot-30-dni.md must exist on classpath")
        assertTrue(resource.contentLength() > 0, "policy/zwrot-30-dni.md must not be empty")
    }
}
