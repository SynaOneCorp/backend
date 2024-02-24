package com.example

import com.example.plugins.*
import com.example.plugins.question.questionRouting
import com.example.plugins.user.userRouting
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

val applicationHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )

    }
}
fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val database = configureDatabases()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity(
        applicationHttpClient = applicationHttpClient,
    )
    configureRouting()

    userRouting(
        database = database,
        applicationHttpClient = applicationHttpClient,
    )

    questionRouting(
        database = database,
        applicationHttpClient = applicationHttpClient,
    )

    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText(text = "404: Page Not Found", status = status)
        }
        status(HttpStatusCode.Unauthorized) { call, status ->
            println("TEST => Hi!")
            call.respondText(text = "401: Unauthorized", status = status)
        }
    }
}
