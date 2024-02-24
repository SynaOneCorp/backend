package com.example.plugins.game.base

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.baseGameRouting(
    baseGameService: BaseGameService,
) {
    routing {
        route("/game") {
            post("") {
                try {
                    val formParameters = call.receive<InBaseGame>()
                    val id = baseGameService
                        .create(
                            exposedGame = formParameters,
                        )
                    call.respond(HttpStatusCode.Created, id)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "game missformed")
                }
            }

            get("/{gameId}") {
                val gameId = call
                    .parameters["gameId"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid gameId")

                call.respond(
                    HttpStatusCode.OK,
                    baseGameService
                        .retrieveGame(
                            gameId = gameId,
                        )
                        ?: "pouet",
                )
            }
        }
    }
}