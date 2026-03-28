package com.lppsa.presentation.html

import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.HEAD
import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.img
import kotlinx.html.lang
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import kotlinx.html.ul

fun renderLandingPage(): String = createHTML().html {
    lang = "pl"
    head { landingHead() }
    body { landingBody() }
}

private fun HEAD.landingHead() {
    meta(charset = "UTF-8")
    meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
    title("Asystent Reklamacji i Zwrotów — Sinsay")
    link(rel = "stylesheet", href = "/css/sinsay.css")
    link(rel = "icon", href = "/logo.svg", type = "image/svg+xml")
}

private fun BODY.landingBody() {
    header(classes = "site-header") {
        a(href = "/", classes = "site-header__logo") {
            attributes["aria-label"] = "Sinsay"
            img(alt = "Sinsay", src = "/logo.svg", classes = "site-header__logo-img") {
                attributes["width"] = "84"
                attributes["height"] = "31"
            }
        }
        span(classes = "badge-wip") { +"W trakcie realizacji" }
    }

    main {
        heroSection()
        featuresSection()
        roadmapSection()
    }

    footer(classes = "site-footer") {
        p { +"© 2026 LPP S.A. — Sinsay. Wszelkie prawa zastrzeżone." }
        hr(classes = "site-footer__divider")
        p {
            +"Kontakt: "
            a(href = "mailto:support.pl@sinsay.com") { +"support.pl@sinsay.com" }
            +" · tel. 58 353 65 65"
        }
        p(classes = "site-footer__disclaimer") {
            +"Aplikacja ma charakter wyłącznie doradczy i nie stanowi oficjalnej decyzji handlowej ani prawnej LPP S.A."
        }
    }
}

private fun FlowContent.heroSection() {
    div(classes = "hero") {
        span(classes = "hero__eyebrow") { +"Nowe narzędzie" }
        h1(classes = "hero__title") {
            span(classes = "hero__title-line") { +"Reklamacja lub zwrot?" }
            span(classes = "hero__title-line") { span { +"Decyzja w kilkanaście sekund." } }
        }
        p(classes = "hero__description") {
            +"Wypełnij jeden formularz, dołącz zdjęcie produktu — AI oceni Twoje zgłoszenie zgodnie z polityką Sinsay bez konieczności dzwonienia na infolinię."
        }
        div(classes = "hero__cta-group") {
            a(href = "/submit", classes = "btn btn--primary") { +"Złóż zgłoszenie" }
            a(href = "#jak-dziala", classes = "btn btn--outline") { +"Jak to działa" }
        }
    }
}

private fun FlowContent.featuresSection() {
    div(classes = "section section--alt") {
        attributes["id"] = "jak-dziala"
        div(classes = "section__inner") {
            h2(classes = "section__heading") { +"Jeden formularz. Decyzja AI. Bez kolejki." }
            p(classes = "section__subheading") {
                +"Przejrzysty proces w trzech krokach — od zgłoszenia do odpowiedzi."
            }
            div(classes = "features-flat-grid") {
                featureItem(
                    symbol = "1",
                    title = "Wypełnij formularz",
                    text = "Wybierz typ zgłoszenia (Reklamacja lub Zwrot), podaj nazwę produktu, datę zakupu, opis problemu i dołącz zdjęcie.",
                )
                featureItem(
                    symbol = "2",
                    title = "AI analizuje zdjęcie",
                    text = "Multimodalny model językowy ocenia zdjęcie i opis w kontekście polityki Sinsay i wydaje wstępną decyzję.",
                )
                featureItem(
                    symbol = "3",
                    title = "Otrzymaj decyzję",
                    text = "W kilkanaście sekund zobaczysz wynik: wniosek wstępnie pozytywny lub negatywny — wraz z uzasadnieniem.",
                )
                featureItem(
                    symbol = "?",
                    title = "Zapytaj o szczegóły",
                    text = "Po decyzji otwiera się czat. Zapytaj o politykę zwrotów, dane kontaktowe lub następne kroki — bez opuszczania strony.",
                )
                featureItem(
                    symbol = "P",
                    title = "Oparte na dokumentach polityki",
                    text = "Każda sesja ładuje właściwe dokumenty: Regulamin, Zasady Reklamacji lub Zasady Zwrotu. Routing jest deterministyczny.",
                )
                featureItem(
                    symbol = "PL",
                    title = "Wyłącznie po polsku",
                    text = "Cały interfejs, komunikaty błędów i odpowiedzi AI są w języku polskim. Aplikacja działa w realiach rynku polskiego.",
                )
            }
        }
    }
}

private fun FlowContent.roadmapSection() {
    div(classes = "section") {
        attributes["id"] = "status"
        div(classes = "section__inner") {
            h2(classes = "section__heading") { +"Postęp prac" }
            p(classes = "section__subheading") {
                +"Projekt jest realizowany iteracyjnie. Poniżej aktualny stan poszczególnych modułów."
            }
            ul(classes = "timeline") {
                timelineItem(status = "done", label = "Architektura systemu i dokumentacja (PRD, ADR, AGENTS)")
                timelineItem(status = "done", label = "Identyfikacja wizualna — tokeny kolorów i typografia")
                timelineItem(status = "done", label = "Konfiguracja projektu: Spring Boot 3 + WebFlux + Kotlin + SQLite")
                timelineItem(status = "done", label = "Formularz przyjmowania zgłoszeń (Reklamacja / Zwrot)")
                timelineItem(status = "done", label = "Integracja z OpenRouter — streaming decyzji AI")
                timelineItem(status = "done", label = "Panel decyzji (ACCEPT / REJECT) z uzasadnieniem")
                timelineItem(status = "done", label = "Czat uzupełniający po decyzji")
                timelineItem(status = "done", label = "Persystencja sesji i wiadomości (SQLite + Flyway)")
                timelineItem(status = "progress", label = "Strona główna (landing page) — redesign")
                timelineItem(status = "planned", label = "Testy integracyjne i E2E")
            }
        }
    }
}

private fun FlowContent.featureItem(
    symbol: String,
    title: String,
    text: String,
) {
    div(classes = "feature-item") {
        div(classes = "feature-item__icon-box") { +symbol }
        div(classes = "feature-item__body") {
            p(classes = "feature-item__title") { +title }
            p(classes = "feature-item__text") { +text }
        }
    }
}

private fun UL.timelineItem(
    status: String,
    label: String,
) {
    val itemClass = when (status) {
        "done"     -> "timeline-item timeline-item--done"
        "progress" -> "timeline-item timeline-item--progress"
        else       -> "timeline-item timeline-item--planned"
    }
    val labelClass = when (status) {
        "done"     -> "timeline-item__label timeline-item__label--done"
        "progress" -> "timeline-item__label timeline-item__label--progress"
        else       -> "timeline-item__label timeline-item__label--planned"
    }
    li(classes = itemClass) {
        span(classes = labelClass) { +label }
    }
}
