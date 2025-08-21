package com.example

import com.example.plugins.configureDefaultHeaders
import com.example.plugins.configureKoin
import com.example.plugins.configureStatusPages
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

fun Application.module() {
    configureKoin()
    configureSerialization()
    configureMonitoring()
    configureRouting()
    configureDefaultHeaders()
    configureStatusPages()
}
