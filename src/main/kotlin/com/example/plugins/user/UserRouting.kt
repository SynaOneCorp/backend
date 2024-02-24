package com.example.plugins.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.userRouting(
    userService: UserService,
) {
    routing {
        post("/user") {
            val user = call.receive<InUser>()
            if (
                userService
                    .isUsernameAvailable(
                        username = user.username,
                    )
            ) {
                userService
                    .create(
                        inUser = user,
                    )
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.Conflict)
            }
        }

        get("/user/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val user = userService.readUserByGoogleId(googleId = id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/user/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            userService.delete(googleId = id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
