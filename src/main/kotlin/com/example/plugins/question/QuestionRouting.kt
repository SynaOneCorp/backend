package com.example.plugins.question

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.questionRouting(
    database: Database,
    applicationHttpClient: HttpClient,
) {
    val questionService = QuestionService(database)
    routing {
        route("/questions") {
            get("/id/{id}") {
                val id = call
                    .parameters["id"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid ID")

                val question = questionService.read(id)
                if (question != null) {
                    call.respond(HttpStatusCode.OK, question)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            get("/random") {
                call.respond(HttpStatusCode.OK, questionService.readRandom())
            }

            post("") {
                try {
                    val formParameters = call.receive<QuestionService.ExposedQuestion>()
                    val existingQuestion = questionService.read(questionToFind = formParameters.question)
                    if (existingQuestion == null) {
                        val id = questionService.create(
                            exposedQuestion = formParameters,
                        )
                        call.respond(HttpStatusCode.Created, id)
                    } else {
                        call.respond(HttpStatusCode.Conflict, "already exists")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "question missformed")
                }
            }

            delete("/{id}") {
                val id = call
                    .parameters["id"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid ID")
                questionService.delete(id)
                call.respond(HttpStatusCode.OK)
            }

            put("/questions/{id}") {
                val id = call
                    .parameters["id"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid ID")
                val question = call.receive<QuestionService.ExposedQuestion>()
                questionService.update(id, question)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}