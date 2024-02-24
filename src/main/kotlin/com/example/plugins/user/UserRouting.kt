package com.example.plugins.user

import com.example.plugins.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.Database

fun Application.userRouting(
    database: Database,
    applicationHttpClient: HttpClient,
) {
    val userService = UserService(database)
    routing {
        get("/register") {
            val userSession: UserSession? = getSession(call)
            if (userSession != null) {
                val (oauthValid, answer) = checkOauth2(
                    httpClient = applicationHttpClient,
                    userSession = userSession,
                )
                if (oauthValid) {
                    try {
                        val account: UserInfo = answer.body()
                        val alreadyTaken = this.call.request.header("available") == "false"
                        call.respondHtml {
                            body {
                                form(
                                    action = "/users",
                                    encType = FormEncType.applicationXWwwFormUrlEncoded,
                                    method = FormMethod.post,
                                ) {
                                    p {
                                        +"Username: "
                                        textInput(name = "username") {
                                            value = account.givenName.orEmpty()
                                        }
                                    }
                                    hiddenInput(name = "googleId") {
                                        value = account.id
                                    }
                                    p {
                                        submitInput { value = "Register!" }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        call
                            .respondRedirect("/login")
                    }
                }
            } else {
                call.respondRedirect("/login")
            }
        }

        // Create user
        post("/users") {
            val formParameters = call.receiveParameters()
            val username = formParameters["username"].toString()
            val googleId = formParameters["googleId"].toString()
            val existingUser = userService.read(username = username)
            println("TEST => $existingUser")
            if (existingUser == null) {
                println("TEST => true")
                val id = userService.create(
                    ExposedUser(
                        username = username,
                        googleId = googleId,
                    )
                )
                call.respond(HttpStatusCode.Created, id)
            } else {
                println("TEST => false")
                call
                    .response
                    .headers
                    .append("available", "false")
                call.respondRedirect("/register")
            }
        }

        // Read user
        get("/users/id/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = userService.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Read user
        get("/users/username/{username}") {
            val username = call.parameters["username"] ?: throw IllegalArgumentException("Invalid USERNAME")
            val user = userService.read(username)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<ExposedUser>()
            userService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }
        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}