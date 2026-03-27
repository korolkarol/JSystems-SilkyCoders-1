package com.lppsa.presentation.web

import com.lppsa.presentation.html.renderLandingPage
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LandingController {

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun landingPage(): String = renderLandingPage()
}
