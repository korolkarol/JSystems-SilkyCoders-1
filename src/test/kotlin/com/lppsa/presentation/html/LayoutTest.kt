package com.lppsa.presentation.html

import kotlinx.html.p
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class LayoutTest {

    @Test
    fun `renderLayout sets Polish language attribute`() {
        val html = renderLayout(title = "Test") {}
        assertTrue(html.contains("lang=\"pl\""), "HTML must have lang=pl")
    }

    @Test
    fun `renderLayout includes sinsay css link`() {
        val html = renderLayout(title = "Test") {}
        assertTrue(html.contains("/css/sinsay.css"), "Must include sinsay.css")
    }

    @Test
    fun `renderLayout includes HTMX script`() {
        val html = renderLayout(title = "Test") {}
        assertTrue(html.contains("htmx"), "Must include HTMX script")
    }

    @Test
    fun `renderLayout includes logo`() {
        val html = renderLayout(title = "Test") {}
        assertTrue(html.contains("/logo.svg"), "Must include logo")
    }

    @Test
    fun `renderLayout includes footer copyright`() {
        val html = renderLayout(title = "Test") {}
        assertTrue(html.contains("Sinsay") && html.contains("LPP"), "Footer must mention Sinsay/LPP")
    }

    @Test
    fun `renderLayout renders content block`() {
        val html = renderLayout(title = "Test") {
            p { +"custom content" }
        }
        assertTrue(html.contains("custom content"), "Content block must be rendered")
    }
}
