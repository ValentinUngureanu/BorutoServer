package com.example

import com.example.plugins.configureDefaultHeaders
import com.example.plugins.configureKoin
import com.example.plugins.configureStatusPages
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

fun Application.module() {
    configureKoin()
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureRouting()
    configureDefaultHeaders()
}
