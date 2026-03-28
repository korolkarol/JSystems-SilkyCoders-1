package com.lppsa.e2e

import com.lppsa.domain.port.EvaluationPort
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.nio.file.Files
import java.nio.file.Path

/**
 * E2E tests for the intake form at GET /.
 *
 * Tests cover:
 * - Form rendering (all required fields present)
 * - Submit button disabled on page load
 * - Submit button enabled after valid file is selected
 * - Form can be filled and submitted (triggers POST /submit)
 *
 * EvaluationPort is mocked via @MockBean so no real OpenRouter API key is required.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntakeFormE2ETest {

    @MockBean
    private lateinit var evaluationPort: EvaluationPort

    @LocalServerPort
    private var port: Int = 8080

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var page: Page

    private val baseUrl get() = "http://localhost:$port"

    @BeforeAll
    fun setUpBrowser() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(
            BrowserType.LaunchOptions().setHeadless(true),
        )
    }

    @AfterAll
    fun tearDownBrowser() {
        browser.close()
        playwright.close()
    }

    @BeforeEach
    fun setUpPage() {
        page = browser.newPage()
    }

    @AfterEach
    fun tearDownPage() {
        page.close()
    }

    @Test
    fun `GET slash renders intake form heading in Polish`() {
        page.navigate(baseUrl)

        assertThat(page.locator("h1.intake-page__heading"))
            .containsText("Zgłoszenie reklamacji lub zwrotu")
    }

    @Test
    fun `GET slash renders Reklamacja and Zwrot radio buttons`() {
        page.navigate(baseUrl)

        val reklamacjaRadio = page.locator("input[name='requestType'][value='REKLAMACJA']")
        val zwrotRadio = page.locator("input[name='requestType'][value='ZWROT']")

        assertThat(reklamacjaRadio).isVisible()
        assertThat(zwrotRadio).isVisible()
    }

    @Test
    fun `GET slash renders radio button labels in Polish`() {
        page.navigate(baseUrl)

        assertThat(page.locator(".intake-form__radio-group")).containsText("Reklamacja")
        assertThat(page.locator(".intake-form__radio-group")).containsText("Zwrot")
    }

    @Test
    fun `GET slash renders product name input`() {
        page.navigate(baseUrl)

        val input = page.locator("input#productName")
        assertThat(input).isVisible()
        assertThat(page.locator("label[for='productName']")).containsText("Nazwa produktu")
    }

    @Test
    fun `GET slash renders purchase date input`() {
        page.navigate(baseUrl)

        val input = page.locator("input#purchaseDate[type='date']")
        assertThat(input).isVisible()
        assertThat(page.locator("label[for='purchaseDate']")).containsText("Data zakupu")
    }

    @Test
    fun `GET slash renders description textarea`() {
        page.navigate(baseUrl)

        val textarea = page.locator("textarea#description")
        assertThat(textarea).isVisible()
        assertThat(page.locator("label[for='description']")).containsText("Opis problemu")
    }

    @Test
    fun `GET slash renders file input accepting image types`() {
        page.navigate(baseUrl)

        val fileInput = page.locator("input#photo[type='file']")
        assertThat(fileInput).isAttached()

        val accept = fileInput.getAttribute("accept")
        assert(accept != null && accept.contains(".jpg")) {
            "File input should accept .jpg, got: $accept"
        }
    }

    @Test
    fun `GET slash renders photo upload label in Polish`() {
        page.navigate(baseUrl)

        assertThat(page.locator("label[for='photo']")).containsText("Dodaj zdjęcie")
    }

    @Test
    fun `GET slash renders decision container div`() {
        page.navigate(baseUrl)

        assertThat(page.locator("div#decision-container")).isAttached()
    }

    @Test
    fun `submit button is disabled on page load`() {
        page.navigate(baseUrl)

        val submitBtn = page.locator("button#submit-btn")
        assertThat(submitBtn).isDisabled()
        assertThat(submitBtn).containsText("WYŚLIJ")
    }

    @Test
    fun `submit button becomes enabled after valid image is selected`() {
        page.navigate(baseUrl)

        val tempImageFile = createMinimalPngTempFile()
        try {
            page.locator("input#photo").setInputFiles(tempImageFile)

            // upload-preview.js should enable the submit button after a valid file is chosen
            val submitBtn = page.locator("button#submit-btn")
            assertThat(submitBtn).isEnabled()
        } finally {
            Files.deleteIfExists(tempImageFile)
        }
    }

    @Test
    fun `form fields can be filled in`() {
        page.navigate(baseUrl)

        page.locator("input[name='requestType'][value='REKLAMACJA']").check()
        page.locator("input#productName").fill("Kurtka zimowa")
        page.locator("input#purchaseDate").fill("2025-01-15")
        page.locator("textarea#description").fill("Zamek błyskawiczny zepsuł się po tygodniu użytkowania.")

        assertThat(page.locator("input[name='requestType'][value='REKLAMACJA']")).isChecked()
        assertThat(page.locator("input#productName")).hasValue("Kurtka zimowa")
        assertThat(page.locator("textarea#description")).hasValue("Zamek błyskawiczny zepsuł się po tygodniu użytkowania.")
    }

    @Test
    fun `filling form and submitting sends POST to submit and updates decision container`() {
        page.navigate(baseUrl)

        val tempImageFile = createMinimalPngTempFile()
        try {
            page.locator("input[name='requestType'][value='REKLAMACJA']").check()
            page.locator("input#productName").fill("Kurtka zimowa")
            page.locator("input#purchaseDate").fill("2025-01-15")
            page.locator("textarea#description").fill("Zamek zepsuł się.")
            page.locator("input#photo").setInputFiles(tempImageFile)

            // Wait for the submit button to be enabled by upload-preview.js
            val submitBtn = page.locator("button#submit-btn")
            assertThat(submitBtn).isEnabled()

            // Configure mock before submit so evaluation doesn't hit real OpenRouter
            given(evaluationPort.evaluate(
                requestType = any(),
                productName = any(),
                purchaseDate = any(),
                description = any(),
                imageBytes = any(),
                imageMimeType = any(),
            )).willReturn(flowOf("ACCEPT ", "Wniosek ", "wstępnie ", "pozytywny."))

            // Intercept POST /submit to capture response status without needing real AI
            val responseRef = arrayOfNulls<com.microsoft.playwright.Response>(1)
            page.onResponse { response ->
                if (response.url().contains("/submit")) {
                    responseRef[0] = response
                }
            }

            submitBtn.click()

            // Wait for HTMX to process the response (decision-container gets updated or error shown)
            page.waitForCondition(
                { responseRef[0] != null },
                Page.WaitForConditionOptions().setTimeout(10_000.0),
            )

            val response = responseRef[0]!!
            assert(response.status() == 200) {
                "Expected POST /submit to return 200 but got ${response.status()}."
            }

            // After fix, decision container should contain some content from the SSE stream
            assertThat(page.locator("#decision-container")).not().isEmpty()
        } finally {
            Files.deleteIfExists(tempImageFile)
        }
    }

    @Test
    fun `form submission shows friendly error when AI service fails`() {
        given(evaluationPort.evaluate(
            requestType = any(),
            productName = any(),
            purchaseDate = any(),
            description = any(),
            imageBytes = any(),
            imageMimeType = any(),
        )).willThrow(RuntimeException("AI unavailable"))

        page.navigate(baseUrl)

        val tempImageFile = createMinimalPngTempFile()
        try {
            page.locator("input[name='requestType'][value='REKLAMACJA']").check()
            page.locator("input#productName").fill("Kurtka zimowa")
            page.locator("input#purchaseDate").fill("2025-01-15")
            page.locator("textarea#description").fill("Zamek się zepsuł.")
            page.locator("input#photo").setInputFiles(tempImageFile)
            assertThat(page.locator("button#submit-btn")).isEnabled()

            page.locator("button#submit-btn").click()

            // Wait for error to appear in decision container
            page.waitForSelector("#decision-container .error-message")

            val errorEl = page.locator("#decision-container .error-message")
            assertThat(errorEl).isVisible()
            assertThat(errorEl).not().isEmpty()

            // Raw JSON error must NOT be visible
            val pageText = page.content()
            assert(!pageText.contains("Internal Server Error")) {
                "Raw 'Internal Server Error' JSON must not be visible to the user"
            }
        } finally {
            Files.deleteIfExists(tempImageFile)
        }
    }

    /**
     * Creates a minimal valid PNG file as a temp file for upload testing.
     * 1x1 pixel transparent PNG — smallest valid PNG binary.
     */
    private fun createMinimalPngTempFile(): Path {
        // 1x1 transparent PNG bytes
        val pngBytes = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk length + type
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, // width=1, height=1
            0x08, 0x02, 0x00, 0x00, 0x00, 0x90.toByte(), 0x77, 0x53, 0xDE.toByte(), // bit depth, color type, CRC
            0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54, // IDAT chunk
            0x08, 0xD7.toByte(), 0x63, 0xF8.toByte(), 0xCF.toByte(), 0xC0.toByte(), 0x00, 0x00,
            0x00, 0x02, 0x00, 0x01, 0xE2.toByte(), 0x21, 0xBC.toByte(), 0x33, // IDAT data + CRC
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE.toByte(), 0x42, 0x60, 0x82.toByte(), // IEND
        )
        val tempFile = Files.createTempFile("test-image", ".png")
        Files.write(tempFile, pngBytes)
        return tempFile
    }
}
