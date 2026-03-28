package com.lppsa.presentation.html

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.textArea

fun renderIntakePage(): String = renderLayout(title = "Zgłoszenie reklamacji lub zwrotu") {
    div(classes = "form-page") {
        div(classes = "form-card") {
            span(classes = "form-card__eyebrow") { +"Zgłoszenie" }
            h1(classes = "intake-page__heading form-card__heading") { +"Zgłoszenie reklamacji lub zwrotu" }
            p(classes = "form-card__subtext") {
                +"Wypełnij formularz, dołącz zdjęcie produktu. Otrzymasz wstępną decyzję w kilkanaście sekund."
            }

            hr(classes = "form-divider")

            formIntake()
        }

        div(classes = "form-decision") {
            id = "decision-container"
        }

        script(src = "/js/upload-preview.js") {}
        script(src = "/js/intake-submit.js") {}
    }
}

private fun FlowContent.formIntake() {
    form(classes = "intake-form") {
        id = "intake-form"

        // Request type toggle
        div(classes = "form-field") {
            span(classes = "form-label") { +"Rodzaj zgłoszenia" }
            div(classes = "type-toggle intake-form__radio-group") {
                label(classes = "type-toggle__wrapper") {
                    input(
                        type = InputType.radio,
                        name = "requestType",
                        classes = "type-toggle__radio",
                    ) {
                        value = "REKLAMACJA"
                        required = true
                    }
                    div(classes = "type-toggle__card") {
                        div(classes = "type-toggle__icon") { +"R" }
                        span(classes = "type-toggle__label") { +"Reklamacja" }
                    }
                }
                label(classes = "type-toggle__wrapper") {
                    input(
                        type = InputType.radio,
                        name = "requestType",
                        classes = "type-toggle__radio",
                    ) {
                        value = "ZWROT"
                    }
                    div(classes = "type-toggle__card") {
                        div(classes = "type-toggle__icon") { +"Z" }
                        span(classes = "type-toggle__label") { +"Zwrot" }
                    }
                }
            }
        }

        // Product name
        div(classes = "form-field") {
            label(classes = "form-label") {
                htmlFor = "productName"
                +"Nazwa produktu"
            }
            input(
                type = InputType.text,
                name = "productName",
                classes = "form-input",
            ) {
                id = "productName"
                placeholder = "Wpisz nazwę produktu"
                required = true
            }
        }

        // Purchase date
        div(classes = "form-field") {
            label(classes = "form-label") {
                htmlFor = "purchaseDate"
                +"Data zakupu"
            }
            input(
                type = InputType.date,
                name = "purchaseDate",
                classes = "form-input",
            ) {
                id = "purchaseDate"
                required = true
            }
        }

        // Description
        div(classes = "form-field") {
            label(classes = "form-label") {
                htmlFor = "description"
                +"Opis problemu"
            }
            textArea(classes = "form-textarea") {
                id = "description"
                name = "description"
                placeholder = "Opisz szczegółowo swój problem"
                required = true
            }
        }

        // Photo upload
        div(classes = "form-field") {
            id = "upload-area"
            label(classes = "form-label") {
                htmlFor = "photo"
                +"Dodaj zdjęcie"
            }
            div(classes = "upload-zone") {
                input(
                    type = InputType.file,
                    name = "image",
                    classes = "upload-zone__input",
                ) {
                    id = "photo"
                    accept = ".jpg,.jpeg,.png,.webp"
                }
                div(classes = "upload-zone__icon") { +"+" }
                p(classes = "upload-zone__text") { +"Przeciągnij i upuść lub kliknij, aby wybrać" }
                p(classes = "upload-zone__hint") { +"Akceptowane: JPG, PNG, WebP" }
            }
            div(classes = "upload-error") {
                id = "file-error"
            }
            div(classes = "upload-preview") {
                id = "image-preview"
            }
        }

        // Submit
        button(
            type = ButtonType.submit,
            classes = "btn btn--primary btn--full",
        ) {
            id = "submit-btn"
            disabled = true
            +"WYŚLIJ"
        }
    }
}
