package com.lppsa.presentation.html

import com.lppsa.domain.model.Decision
import com.lppsa.presentation.html.components.Htmx.hxPost
import com.lppsa.presentation.html.components.Htmx.hxSwap
import com.lppsa.presentation.html.components.Htmx.hxTarget
import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun renderDecisionPanel(
    sessionId: String,
    decision: Decision,
): String = createHTML().div(classes = "decision-panel") {
    when (decision) {
        is Decision.Accept -> {
            div(classes = "decision-panel__status decision-panel__status--accept") {
                style = "color:#0DB209;font-weight:600;"
                +"Wniosek wstępnie pozytywny"
            }
            div(classes = "decision-panel__explanation") {
                +decision.explanation
            }
            if (decision.nextSteps.isNotBlank()) {
                div(classes = "decision-panel__next-steps") {
                    +decision.nextSteps
                }
            }
        }
        is Decision.Reject -> {
            div(classes = "decision-panel__status decision-panel__status--reject") {
                style = "color:#FF0023;font-weight:600;"
                +"Wniosek wstępnie negatywny"
            }
            div(classes = "decision-panel__explanation") {
                +decision.explanation
            }
            if (decision.mismatchRecommendation != null) {
                div(classes = "decision-panel__mismatch") {
                    +decision.mismatchRecommendation
                }
            }
        }
    }

    div(classes = "decision-panel__chat") {
        div(classes = "chat-messages") {
            id = "chat-messages"
        }

        form(classes = "chat-form") {
            hxPost("/chat/$sessionId")
            hxTarget("#chat-messages")
            hxSwap("beforeend")

            input(type = InputType.text, name = "message", classes = "chat-form__input") {
                placeholder = "Twoje pytanie..."
                required = true
            }
            button(type = ButtonType.submit, classes = "chat-form__submit") {
                style = "background-color:#E09243;border-radius:0;color:#FFFFFF;"
                +"WYŚLIJ"
            }
        }
    }
}
