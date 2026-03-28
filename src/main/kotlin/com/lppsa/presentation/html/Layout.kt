package com.lppsa.presentation.html

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun renderLayout(title: String, content: FlowContent.() -> Unit): String = createHTML().html {
    lang = "pl"
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title(title)
        link(rel = "stylesheet", href = "/css/sinsay.css")
        script(src = "https://unpkg.com/htmx.org@2.0.4") {}
        script(src = "https://unpkg.com/htmx-ext-sse@2.2.2/sse.js") {}
    }
    body {
        header {
            classes = setOf("site-header")
            a(href = "/") {
                classes = setOf("brand-logo-button")
                attributes["aria-label"] = "Sinsay"
                img(src = "/logo.svg", alt = "Sinsay") {
                    width = "84"
                    height = "31"
                }
            }
        }
        main {
            content()
        }
        footer {
            classes = setOf("site-footer")
            p { +"© 2026 Sinsay / LPP S.A. Wszelkie prawa zastrzeżone." }
        }
    }
}
