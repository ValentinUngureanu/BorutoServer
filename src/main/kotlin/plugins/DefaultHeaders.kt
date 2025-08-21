package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*
import java.time.Duration

fun Application.configureDefaultHeaders() {
    install(DefaultHeaders) {
        val oneYearInSeconds = Duration.ofDays(365).toSeconds()
        header(name = HttpHeaders.CacheControl, value = "public, max-age=  $oneYearInSeconds, immutable")
    }
}
