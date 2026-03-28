package com.lppsa.infrastructure.ai

import com.lppsa.domain.model.ChatMessage
import com.lppsa.domain.model.MessageRole
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import com.lppsa.domain.port.EvaluationPort
import com.lppsa.infrastructure.config.AiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.content.Media
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import org.springframework.util.MimeType

@Component
class SpringAiEvaluationAdapter(
    private val chatClient: ChatClient,
    private val aiConfig: AiConfig,
) : EvaluationPort {

    override fun evaluate(
        requestType: RequestType,
        productName: String,
        purchaseDate: String,
        description: String,
        imageBytes: ByteArray,
        imageMimeType: String,
    ): Flow<String> {
        val systemPrompt = buildEvaluationSystemPrompt(
            requestType = requestType,
            policy = aiConfig.loadPolicy(requestType),
        )
        val media = Media(MimeType.valueOf(imageMimeType), ByteArrayResource(imageBytes))
        val userPrompt = buildEvaluationUserPrompt(
            productName = productName,
            purchaseDate = purchaseDate,
            description = description,
        )
        return chatClient.prompt()
            .system(systemPrompt)
            .user { it.text(userPrompt).media(media) }
            .stream()
            .content()
            .asFlow()
    }

    override fun chat(
        session: Session,
        history: List<ChatMessage>,
        userMessage: String,
    ): Flow<String> {
        val systemPrompt = buildChatSystemPrompt(
            requestType = session.requestType,
            policy = aiConfig.loadPolicy(session.requestType),
            decision = session.decision,
        )
        val messages: List<Message> = history.map { msg ->
            when (msg.role) {
                MessageRole.USER -> UserMessage(msg.content)
                MessageRole.ASSISTANT -> AssistantMessage(msg.content)
            }
        }
        return chatClient.prompt()
            .system(systemPrompt)
            .messages(messages)
            .user(userMessage)
            .stream()
            .content()
            .asFlow()
    }

    private fun buildEvaluationSystemPrompt(
        requestType: RequestType,
        policy: String,
    ): String {
        val requestTypeName = when (requestType) {
            RequestType.REKLAMACJA -> "Reklamacja"
            RequestType.ZWROT -> "Zwrot"
        }
        return """
            Jesteś asystentem obsługi klienta sklepu Sinsay. Oceniasz wnioski klientów na podstawie polityki sklepu.
            Odpowiadaj WYŁĄCZNIE po polsku.

            Rodzaj zgłoszenia: $requestTypeName

            Polityka sklepu:
            $policy

            Instrukcje oceny:
            1. Przeanalizuj dostarczone zdjęcie, opis produktu i datę zakupu.
            2. Oceń wniosek zgodnie z polityką sklepu.
            3. Na początku odpowiedzi umieść DOKŁADNIE jedną z poniższych linii:
               - "ACCEPT: Wniosek wstępnie pozytywny" — jeśli wniosek spełnia warunki
               - "REJECT: Wniosek wstępnie negatywny" — jeśli wniosek nie spełnia warunków
            4. Wyjaśnienie musi być zwięzłe — maksymalnie 200 słów.
            5. Przy akceptacji podaj kolejne kroki dla klienta.
            6. Przy odrzuceniu podaj przyczynę odrzucenia.
            7. Jeśli wykryjesz niezgodność (np. klient chce zwrotu, a zgłosił reklamację), zaznacz to wyraźnie.
            8. Nie inicjuj żadnych działań w systemach backendowych.
        """.trimIndent()
    }

    private fun buildEvaluationUserPrompt(
        productName: String,
        purchaseDate: String,
        description: String,
    ): String = """
        Produkt: $productName
        Data zakupu: $purchaseDate
        Opis problemu: $description

        Proszę o ocenę mojego wniosku wraz z analizą załączonego zdjęcia.
    """.trimIndent()

    private fun buildChatSystemPrompt(
        requestType: RequestType,
        policy: String,
        decision: com.lppsa.domain.model.Decision?,
    ): String {
        val requestTypeName = when (requestType) {
            RequestType.REKLAMACJA -> "Reklamacja"
            RequestType.ZWROT -> "Zwrot"
        }
        val decisionContext = when (decision) {
            is com.lppsa.domain.model.Decision.Accept ->
                "Wstępna decyzja: POZYTYWNA\nWyjaśnienie: ${decision.explanation}\nKolejne kroki: ${decision.nextSteps}"
            is com.lppsa.domain.model.Decision.Reject ->
                "Wstępna decyzja: NEGATYWNA\nPrzyczyna: ${decision.explanation}"
            null -> "Decyzja jeszcze nie wydana."
        }
        return """
            Jesteś asystentem obsługi klienta sklepu Sinsay. Odpowiadasz na pytania dotyczące zgłoszenia klienta.
            Odpowiadaj WYŁĄCZNIE po polsku. Bądź pomocny i rzeczowy.

            Rodzaj zgłoszenia: $requestTypeName
            $decisionContext

            Polityka sklepu:
            $policy

            Odpowiadaj wyłącznie na pytania związane z tym zgłoszeniem. Jeśli pytanie jest niezwiązane z tematem,
            grzecznie poinformuj klienta, że możesz pomóc tylko w sprawach dotyczących tego zgłoszenia.
        """.trimIndent()
    }
}
