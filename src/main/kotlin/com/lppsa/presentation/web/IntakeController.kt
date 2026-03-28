package com.lppsa.presentation.web

import com.lppsa.application.usecase.SubmitCommand
import com.lppsa.application.usecase.SubmitRequestUseCase
import com.lppsa.domain.model.RequestType
import com.lppsa.presentation.html.renderIntakePage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.Part
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

private const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024L
private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")

@RestController
class IntakeController(
    private val submitRequestUseCase: SubmitRequestUseCase,
) {

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun intakePage(): String = renderIntakePage()

    @PostMapping(
        "/submit",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    suspend fun submit(
        @RequestPart("requestType") requestType: String,
        @RequestPart("productName") productName: String,
        @RequestPart("purchaseDate") purchaseDate: String,
        @RequestPart("description") description: String,
        @RequestPart("image", required = false) imagePart: Part?,
        @RequestPart("imageMimeType", required = false) imageMimeType: String?,
    ): Flow<String> {
        if (requestType.isBlank() || productName.isBlank() || purchaseDate.isBlank() || description.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Wszystkie pola są wymagane.",
            )
        }

        if (imagePart == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Zdjęcie produktu jest wymagane.",
            )
        }

        val contentType = imagePart.headers().contentType?.toString()
            ?: imageMimeType
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie można określić typu pliku.")

        if (contentType !in ALLOWED_MIME_TYPES) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nieobsługiwany format pliku. Akceptowane: JPG, PNG, WEBP.",
            )
        }

        val imageBytes = imagePart.content()
            .reduce { acc, buf ->
                val combined = acc.factory().allocateBuffer(acc.readableByteCount() + buf.readableByteCount())
                combined.write(acc)
                combined.write(buf)
                combined
            }
            .map { buf ->
                val bytes = ByteArray(buf.readableByteCount())
                buf.read(bytes)
                bytes
            }
            .awaitSingle()

        if (imageBytes.size > MAX_IMAGE_SIZE_BYTES) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Plik jest za duży. Maksymalny rozmiar to 10 MB.",
            )
        }

        val parsedRequestType = try {
            RequestType.valueOf(requestType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Nieprawidłowy typ zgłoszenia.")
        }

        val command = SubmitCommand(
            requestType = parsedRequestType,
            productName = productName,
            purchaseDate = purchaseDate,
            description = description,
            imageBytes = imageBytes,
            imageMimeType = contentType,
        )

        val (_, flow) = submitRequestUseCase.execute(command)
        return flow
    }
}
