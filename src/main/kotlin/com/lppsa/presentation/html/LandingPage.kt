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
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.hr
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
        statusSection()
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
        span(classes = "hero__eyebrow") { +"Nowe narzędzie Sinsay" }
        h1(classes = "hero__title") {
            +"Asystent "
            span { +"Reklamacji i Zwrotów" }
        }
        p(classes = "hero__description") {
            +"Inteligentny czat AI, który pomaga klientom Sinsay szybko ocenić, czy zgłoszenie reklamacyjne "
            +"lub wniosek o zwrot spełnia warunki polityki sklepu — bez konieczności dzwonienia na infolinię."
        }
        div(classes = "hero__cta-group") {
            a(href = "#funkcje", classes = "btn btn--primary") { +"Dowiedz się więcej" }
            a(href = "#status", classes = "btn btn--outline") { +"Status projektu" }
        }
    }
}

private fun FlowContent.statusSection() {
    div(classes = "section") {
        attributes["id"] = "status"
        div(classes = "section__inner") {
            div(classes = "status-banner") {
                span(classes = "status-banner__icon") { +"⚙" }
                div {
                    p(classes = "status-banner__title") { +"Aplikacja jest w trakcie budowy" }
                    p(classes = "status-banner__text") {
                        +"Aktualnie pracujemy nad pełną implementacją systemu. "
                        +"Formularz przyjmowania zgłoszeń, ocena AI oraz czat uzupełniający są w fazie rozwoju. "
                        +"Docelowa wersja będzie dostępna wkrótce."
                    }
                }
            }
        }
    }
}

private fun FlowContent.featuresSection() {
    div(classes = "section section--alt") {
        attributes["id"] = "funkcje"
        div(classes = "section__inner") {
            h2(classes = "section__heading") { +"Co zaoferuje aplikacja?" }
            p(classes = "section__subheading") { +"Jeden formularz. Zdjęcie produktu. Decyzja AI w kilkanaście sekund." }
            div(classes = "features-grid") {
                featureCard(
                    icon = "📋",
                    title = "Ustrukturyzowany formularz",
                    text = "Klient podaje typ zgłoszenia (Reklamacja lub Zwrot), nazwę produktu, " +
                        "datę zakupu, opis problemu i zdjęcie — wszystko w jednym miejscu.",
                )
                featureCard(
                    icon = "🤖",
                    title = "Ocena AI z analizą zdjęcia",
                    text = "Multimodalny model językowy analizuje zdjęcie i opis w kontekście polityki Sinsay " +
                        "i wydaje wstępną decyzję: Wniosek wstępnie pozytywny lub Wniosek wstępnie negatywny.",
                )
                featureCard(
                    icon = "💬",
                    title = "Czat uzupełniający",
                    text = "Po decyzji otwiera się czat, w którym klient może zapytać o szczegóły polityki, " +
                        "dane kontaktowe Sinsay lub następne kroki — bez opuszczania strony.",
                )
                featureCard(
                    icon = "🔀",
                    title = "Wykrywanie niezgodności",
                    text = "Jeśli wybrany typ zgłoszenia nie pasuje do opisu, asystent wskazuje właściwy tryb " +
                        "— reklamacja zamiast zwrotu lub odwrotnie.",
                )
                featureCard(
                    icon = "📜",
                    title = "Oparte na dokumentach polityki",
                    text = "Każda sesja ładuje odpowiednie dokumenty: Regulamin, Zasady Reklamacji lub " +
                        "Zasady Zwrotu w ciągu 30 dni. Routing jest deterministyczny — bez mieszania zasad.",
                )
                featureCard(
                    icon = "🇵🇱",
                    title = "Wyłącznie po polsku",
                    text = "Cały interfejs, komunikaty błędów i odpowiedzi AI są w języku polskim. " +
                        "Aplikacja działa w pełni w realiach rynku polskiego.",
                )
            }
        }
    }
}

private fun FlowContent.roadmapSection() {
    div(classes = "section") {
        div(classes = "section__inner") {
            h2(classes = "section__heading") { +"Postęp prac" }
            p(classes = "section__subheading") {
                +"Projekt jest realizowany iteracyjnie. Poniżej aktualny stan poszczególnych modułów."
            }
            ul(classes = "roadmap-list") {
                roadmapItem(status = "done", label = "Architektura systemu i dokumentacja (PRD, ADR, AGENTS)")
                roadmapItem(status = "done", label = "Identyfikacja wizualna — tokeny kolorów i typografia (BRANDING)")
                roadmapItem(status = "done", label = "Konfiguracja projektu: Spring Boot 3 + WebFlux + Kotlin + SQLite")
                roadmapItem(status = "progress", label = "Strona główna (landing page) — ta strona")
                roadmapItem(status = "planned", label = "Formularz przyjmowania zgłoszeń (Reklamacja / Zwrot)")
                roadmapItem(status = "planned", label = "Integracja z OpenRouter — streaming decyzji AI")
                roadmapItem(status = "planned", label = "Panel decyzji (ACCEPT / REJECT) z uzasadnieniem")
                roadmapItem(status = "planned", label = "Czat uzupełniający po decyzji")
                roadmapItem(status = "planned", label = "Persystencja sesji i wiadomości (SQLite + Flyway)")
                roadmapItem(status = "planned", label = "Testy integracyjne i E2E")
            }
        }
    }
}

private fun FlowContent.featureCard(
    icon: String,
    title: String,
    text: String,
) {
    div(classes = "feature-card") {
        div(classes = "feature-card__icon") { +icon }
        h3(classes = "feature-card__title") { +title }
        p(classes = "feature-card__text") { +text }
    }
}

private fun UL.roadmapItem(
    status: String,
    label: String,
) {
    val statusClass = when (status) {
        "done"     -> "roadmap-item__status--done"
        "progress" -> "roadmap-item__status--progress"
        else       -> "roadmap-item__status--planned"
    }
    val statusMark = when (status) {
        "done"     -> "✓"
        "progress" -> "…"
        else       -> "·"
    }
    val labelClass = if (status == "planned") "roadmap-item__label--planned" else "roadmap-item__label"
    li(classes = "roadmap-item") {
        span(classes = "roadmap-item__status $statusClass") { +statusMark }
        span(classes = labelClass) { +label }
    }
}
