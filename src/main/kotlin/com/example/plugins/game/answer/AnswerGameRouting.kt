package com.example.plugins.game.answer

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.answerGameRouting(
    answerGameService: AnswerGameService,
) {
    routing {
        route("/game") {
            post("/{gameId}/{gameQuestionId}/addAnswer") {
                val gameQuestionId = call
                    .parameters["gameQuestionId"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid gameQuestionId")

                val formParameters = call.receive<InAnswerGame>()

                call.respond(
                    HttpStatusCode.Created,
                    answerGameService
                        .addAnswer(
                            questionGameIdValue = gameQuestionId,
                            answer = formParameters.answer,
                            wrapInDbQuery = true,
                        ),
                )
            }

            get("/{gameId}/{questionGameId}") {
                val questionGameId = call
                    .parameters["questionGameId"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid gameQuestionId")

                call.respond(
                    HttpStatusCode.Created,
                    answerGameService
                        .readAnswers(
                            questionGameIdValue = questionGameId,
                        )
                )
            }

            put("/{gameId}/{questionGameId}/{answerQuestionGameId}") {
                val questionGameId = call
                    .parameters["questionGameId"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid questionGameId")
                val answerQuestionGameId = call
                    .parameters["answerQuestionGameId"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid answerQuestionGameId")

                val formParameters = call.receive<InAnswerGame>()

                call.respond(
                    HttpStatusCode.OK,
                    answerGameService
                        .replaceAnswer(
                            questionGameIdValue = questionGameId,
                            answerQuestionGameId = answerQuestionGameId,
                            answer = formParameters.answer,
                        ),
                )

            }
        }
    }
}