package com.example.plugins

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun Application.configureSecurity(
    applicationHttpClient: HttpClient,
) {

    data class MySession(val count: Int = 0)
    val redirects = mutableMapOf<String, String>()
    install(Sessions) {
        cookie<MySession>("my_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 600
            cookie.extensions["SameSite"] = "lax"
        }
        cookie<UserSession>("user_session")
    }
    install(Authentication) {
        oauth("auth-oauth-google") {
            urlProvider = { "http://synaonebackend.onrender.com/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = this@configureSecurity.environment.config.propertyOrNull("ktor.environment.oauthClient")!!.getString(),
                    clientSecret = this@configureSecurity.environment.config.propertyOrNull("ktor.environment.oauthSecret")!!.getString(),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile"),
                    onStateCreated = { call, state ->
                        //saves new state with redirect url value
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = "http://synaonebackend.onrender.com/callback"
                        }
                    }
                )
            }
            client = applicationHttpClient
        }
    }
    routing {
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }

        authenticate("auth-oauth-google") {
            get("login") { }

            get("/callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                currentPrincipal?.state?.let { state ->
                    call.sessions.set(UserSession(state = state, accessToken = currentPrincipal.accessToken))
                    redirects[state]?.let { redirect ->
                        call.respondRedirect(redirect)
                        return@get
                    }
                }
                call.respondRedirect("/register")
            }
        }

        get("/session/refresh") {
            val userSession: UserSession? = getSession(call)

            if (userSession != null) {
                val (oauthValid, answer) = checkOauth2(
                    httpClient = applicationHttpClient,
                    userSession = userSession,
                )
            }
        }

        get("/toto") {
            call.respondHtml {
                body {
                    p {
                        a("/login") { +"Login with Google" }
                    }
                }
            }
        }
        get("/{path}") {
            val userSession: UserSession? = getSession(call)
            if (userSession != null) {
                val userInfo = getPersonalGreeting(applicationHttpClient, userSession)
                call.respondText("Hello, $userInfo")//${userInfo.name}! [$userInfo]")
            }
        }


    }
}

data class UserSession(
    val state: String,
    val accessToken: String,
)

@Serializable
data class Toto(
    val id: String,
)

suspend fun PipelineContext<Unit, ApplicationCall>.checkOauth2(
    httpClient: HttpClient,
    userSession: UserSession,
): Pair<Boolean, HttpResponse> {
    val answer = httpClient
        .get("https://www.googleapis.com/oauth2/v2/userinfo") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${userSession.accessToken}")
            }
        }
    return when (answer.status) {
        HttpStatusCode.OK -> {
            val test = answer.body<Toto>()
            println("NMO: $test")
            Pair(true, answer)
        }
        else -> {
            println("LOG: fail to retrieve userinfo (${userSession.accessToken}): ${answer.bodyAsText()}")
            call.respond(answer.status)
            Pair(false, answer)
        }
    }
}

private suspend fun getPersonalGreeting(
    httpClient: HttpClient,
    userSession: UserSession
)/*: UserInfo*/ = httpClient
    .get("https://www.googleapis.com/oauth2/v2/userinfo") {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${userSession.accessToken}")
        }
    }
    .bodyAsText()

        //.body()

suspend fun getSession(
    call: ApplicationCall
): UserSession? {
    val userSession: UserSession? = call.sessions.get()
    //if there is no session, redirect to login
    if (userSession == null) {
        val redirectUrl = URLBuilder("http://0.0.0.0:8080/login").run {
            parameters.append("redirectUrl", call.request.uri)
            build()
        }
        call.respondRedirect(redirectUrl)
        return null
    }
    return userSession
}

@Serializable
data class UserInfo(
    val id: String,
//    val name: String,
    @SerialName("given_name") val givenName: String?,
//    val picture: String?,
//    val locale: String?,
)