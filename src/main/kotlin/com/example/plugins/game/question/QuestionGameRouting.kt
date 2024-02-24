package com.example.plugins.game.question

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.questionGameRouting(
    questionGameService: QuestionGameService,
) {
    routing {
        route("/game") {
            get("/{gameId}/newQuestion") {
                val gameId = call
                    .parameters["gameId"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid gameId")
                val id = questionGameService
                    .newQuestion(
                        gameIdValue = gameId,
                    )
                call.respond(HttpStatusCode.Created, id)
            }
        }
    }
}