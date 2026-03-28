package com.lppsa.presentation.html

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class IntakePageTest {

    private val html: String by lazy { renderIntakePage() }

    @Test
    fun `renderIntakePage includes main heading`() {
        assertTrue(
            actual = html.contains("Zgłoszenie reklamacji lub zwrotu"),
            message = "Must contain H1 heading",
        )
    }

    @Test
    fun `renderIntakePage includes radio input for Reklamacja`() {
        assertTrue(
            actual = html.contains("Reklamacja"),
            message = "Must contain Reklamacja radio option",
        )
        assertTrue(
            actual = html.contains("name=\"requestType\""),
            message = "Radio inputs must use name=requestType",
        )
    }

    @Test
    fun `renderIntakePage includes radio input for Zwrot`() {
        assertTrue(
            actual = html.contains("Zwrot"),
            message = "Must contain Zwrot radio option",
        )
    }

    @Test
    fun `renderIntakePage includes text input for productName`() {
        assertTrue(
            actual = html.contains("name=\"productName\""),
            message = "Must contain productName input",
        )
    }

    @Test
    fun `renderIntakePage includes date input for purchaseDate`() {
        assertTrue(
            actual = html.contains("name=\"purchaseDate\""),
            message = "Must contain purchaseDate input",
        )
        assertTrue(
            actual = html.contains("type=\"date\""),
            message = "purchaseDate must be date type",
        )
    }

    @Test
    fun `renderIntakePage includes textarea for description`() {
        assertTrue(
            actual = html.contains("name=\"description\""),
            message = "Must contain description textarea",
        )
    }

    @Test
    fun `renderIntakePage includes file upload input`() {
        assertTrue(
            actual = html.contains("type=\"file\""),
            message = "Must contain file input",
        )
        assertTrue(
            actual = html.contains(".jpg") || html.contains("image/jpeg"),
            message = "File input must accept jpg/jpeg",
        )
    }

    @Test
    fun `renderIntakePage includes submit button with Polish label`() {
        assertTrue(
            actual = html.contains("WYŚLIJ"),
            message = "Submit button must say WYŚLIJ",
        )
    }

    @Test
    fun `renderIntakePage form has hx-post attribute`() {
        assertTrue(
            actual = html.contains("hx-post=\"/submit\""),
            message = "Form must have hx-post=/submit",
        )
    }

    @Test
    fun `renderIntakePage includes decision container div`() {
        assertTrue(
            actual = html.contains("id=\"decision-container\""),
            message = "Must contain decision-container div",
        )
    }

    @Test
    fun `renderIntakePage includes upload-preview script`() {
        assertTrue(
            actual = html.contains("/js/upload-preview.js"),
            message = "Must include upload-preview.js script",
        )
    }
}
