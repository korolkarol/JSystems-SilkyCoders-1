package com.lppsa.presentation.html

import kotlinx.html.*

fun renderIntakePage(): String = renderLayout(title = "Zgłoszenie reklamacji lub zwrotu") {
    div(classes = "intake-page") {
        h1(classes = "intake-page__heading") {
            +"Zgłoszenie reklamacji lub zwrotu"
        }

        form(classes = "intake-form") {
            id = "intake-form"

            div(classes = "intake-form__field") {
                p(classes = "intake-form__label") {
                    +"Rodzaj zgłoszenia"
                }
                div(classes = "intake-form__radio-group") {
                    label(classes = "intake-form__radio-label") {
                        input(type = InputType.radio, name = "requestType", classes = "intake-form__radio") {
                            value = "REKLAMACJA"
                            required = true
                        }
                        +"Reklamacja"
                    }
                    label(classes = "intake-form__radio-label") {
                        input(type = InputType.radio, name = "requestType", classes = "intake-form__radio") {
                            value = "ZWROT"
                        }
                        +"Zwrot"
                    }
                }
            }

            div(classes = "intake-form__field") {
                label(classes = "intake-form__label") {
                    htmlFor = "productName"
                    +"Nazwa produktu"
                }
                input(type = InputType.text, name = "productName", classes = "intake-form__input") {
                    id = "productName"
                    placeholder = "Wpisz nazwę produktu"
                    required = true
                }
            }

            div(classes = "intake-form__field") {
                label(classes = "intake-form__label") {
                    htmlFor = "purchaseDate"
                    +"Data zakupu"
                }
                input(type = InputType.date, name = "purchaseDate", classes = "intake-form__input") {
                    id = "purchaseDate"
                    required = true
                }
            }

            div(classes = "intake-form__field") {
                label(classes = "intake-form__label") {
                    htmlFor = "description"
                    +"Opis problemu"
                }
                textArea(classes = "intake-form__textarea") {
                    id = "description"
                    name = "description"
                    placeholder = "Opisz szczegółowo swój problem"
                    required = true
                }
            }

            div(classes = "intake-form__field intake-form__upload-area") {
                id = "upload-area"
                label(classes = "intake-form__label") {
                    htmlFor = "photo"
                    +"Dodaj zdjęcie"
                }
                div(classes = "intake-form__drop-zone") {
                    input(type = InputType.file, name = "image", classes = "intake-form__file-input") {
                        id = "photo"
                        accept = ".jpg,.jpeg,.png,.webp"
                    }
                    p(classes = "intake-form__drop-hint") {
                        +"Przeciągnij i upuść zdjęcie lub kliknij, aby wybrać"
                    }
                }
                div(classes = "intake-form__file-error") {
                    id = "file-error"
                }
                div(classes = "intake-form__preview") {
                    id = "image-preview"
                }
            }

            button(type = ButtonType.submit, classes = "intake-form__submit") {
                id = "submit-btn"
                disabled = true
                style = "background-color:#E09243;border-radius:0;"
                +"WYŚLIJ"
            }
        }

        div {
            id = "decision-container"
        }

        script(src = "/js/upload-preview.js") {}
        script(src = "/js/intake-submit.js") {}
    }
}
