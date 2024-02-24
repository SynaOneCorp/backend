package com.example.plugins.question

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.questionRouting(
    questionService: QuestionService,
) {
    routing {
        route("/question") {
            get("/id/{id}") {
                val id = call
                    .parameters["id"]
                    ?.toInt()
                    ?: throw IllegalArgumentException("Invalid ID")

                val question = questionService
                    .read(id)
                if (question != null) {
                    call.respond(HttpStatusCode.OK, question)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            get("") {
                call.respond(HttpStatusCode.OK, questionService.readAll())
            }

            get("/random") {
                call.respond(HttpStatusCode.OK, questionService.readRandom())
            }

            post("") {
                try {
                    val inQuestion = call.receive<InQuestion>()
                    val existingQuestion = questionService.read(questionToFind = inQuestion.question)
                    if (existingQuestion == null) {
                        val id = questionService
                            .create(
                                inQuestion = inQuestion,
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
//                val id = call
//                    .parameters["id"]
//                    ?.toInt()
//                    ?: throw IllegalArgumentException("Invalid ID")
//                val question = call.receive<QuestionService.InExposedQuestion>()
//                questionService.update(id, question)
//                call.respond(HttpStatusCode.OK)
            }
        }
    }
}