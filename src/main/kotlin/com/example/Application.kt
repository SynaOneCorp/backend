package com.example

import com.example.plugins.*
import com.example.plugins.answer.AnswerService
import com.example.plugins.game.answer.AnswerGameService
import com.example.plugins.game.answer.answerGameRouting
import com.example.plugins.game.base.BaseGameService
import com.example.plugins.game.base.baseGameRouting
import com.example.plugins.game.question.QuestionGameService
import com.example.plugins.game.question.questionGameRouting
import com.example.plugins.question.QuestionService
import com.example.plugins.question.questionRouting
import com.example.plugins.user.UserService
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
    configureRouting()

    val userService = UserService(
        database = database,
        applicationHttpClient = applicationHttpClient,
    )
    userRouting(
        userService = userService,
    )

    val answerService = AnswerService(database)
    val questionService = QuestionService(database, answerService)
    questionRouting(
        questionService = questionService,
    )
    val answerGameService = AnswerGameService(database)
    answerGameRouting(
        answerGameService = answerGameService,
    )
    val baseGameService = BaseGameService(database, questionService, answerGameService)
    baseGameRouting(
        baseGameService = baseGameService,
    )
    val questionGameService = QuestionGameService(database, questionService, answerGameService)
    questionGameRouting(
        questionGameService = questionGameService,
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
