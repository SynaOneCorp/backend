package com.example.plugins.user

import com.example.applicationHttpClient
import com.example.plugins.common.dbQuery
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserService(
    database: Database,
    applicationHttpClient: HttpClient,
) {
    object User : Table() {
        val googleId: Column<String> = varchar("googleId", 250)
        val username: Column<String> = varchar("username", 250)
        val oAuthToken: Column<String> = varchar("oAuthToken", 500)

        override val primaryKey = PrimaryKey(googleId)
    }

    companion object {
        private const val USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
    }
    init {
        transaction(database) {
            try {
                SchemaUtils.dropDatabase()
                SchemaUtils
                    .drop(User)
                SchemaUtils
                    .create(User)
            } catch (e: Exception) {
                println("User table already exists")
            }
        }
    }

    suspend fun isUsernameAvailable(
        username: String,
        wrapInDbQuery: Boolean = true,
    ): Boolean {
        return readUserByUsername(
            username = username,
            wrapInDbQuery = wrapInDbQuery,
        ) == null
    }

    suspend fun isTokenValidAndUserExists(
        oAuthToken: String?,
    ): OutUser? {
        return try {
            require(oAuthToken != null)
            val isOAuthValid = checkOauth2Remotely(
                oAuthToken = oAuthToken,
            )
            if (isOAuthValid.first) {
                val user = isOAuthValid
                    .second
                    .body<UserInfoResponse>()
                readUserByGoogleId(
                    googleId = user.id,
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isTokenValid(
        oAuthToken: String?,
    ): UserInfoResponse? {
        return try {
            require(oAuthToken != null)
            val isOAuthValid = checkOauth2Remotely(
                oAuthToken = oAuthToken,
            )
            if (isOAuthValid.first) {
                isOAuthValid
                    .second
                    .body<UserInfoResponse>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun create(
        inUser: InUser,
    ): String = dbQuery {
        val googleId = User
            .insert {
                it[this.googleId] = inUser.googleId
                it[this.oAuthToken] = inUser.oAuthToken
                it[this.username] = inUser.username
            }[User.googleId]
        googleId
    }

    suspend fun readUserByOAuthToken(
        oAuthToken: String,
        wrapInDbQuery: Boolean = true,
    ): OutUser? = if (wrapInDbQuery) {
        dbQuery {
            readUserByOAuthToken(
                oAuthToken = oAuthToken,
            )
        }
    } else {
        readUserByOAuthToken(
            oAuthToken = oAuthToken,
        )
    }

    private suspend fun readUserByUsername(
        username: String,
        wrapInDbQuery: Boolean = true,
    ): OutUser? = if (wrapInDbQuery) {
        dbQuery {
            readUserByUsername(
                username = username,
            )
        }
    } else {
        readUserByUsername(
            username = username,
        )
    }

    suspend fun readAll(
        wrapInDbQuery: Boolean = true,
    ): List<OutUser> = if (wrapInDbQuery) {
        dbQuery {
            readAll()
        }
    } else {
        readAll()
    }

    suspend fun delete(
        googleId: String,
        wrapInDbQuery: Boolean = true,
    ) = if (wrapInDbQuery) {
        dbQuery {
            delete(
                googleId = googleId,
            )
        }
    } else {
        delete(
            googleId = googleId,
        )
    }

    suspend fun updateOAuthToken(
        googleId: String,
        oAuthToken: String,
        wrapInDbQuery: Boolean = true,
    ) = if (wrapInDbQuery) {
        dbQuery {
            updateOAuthToken(
                googleId = googleId,
                oAuthToken = oAuthToken,
            )
        }
    } else {
        updateOAuthToken(
            googleId = googleId,
            oAuthToken = oAuthToken,
        )
    }

    private suspend fun checkOauth2Remotely(
        oAuthToken: String,
    ): Pair<Boolean, HttpResponse> {
        val answer = applicationHttpClient
            .get(
                urlString = USER_INFO_URL,
            ) {
                headers {
                    append(
                        name = HttpHeaders.Authorization,
                        value = "Bearer $oAuthToken",
                    )
                }
            }
        return when (answer.status) {
            HttpStatusCode.OK ->
                Pair(true, answer)
            else -> {
                println("LOG: fail to retrieve userinfo ($oAuthToken): ${answer.bodyAsText()}")
                Pair(false, answer)
            }
        }
    }

    private fun updateOAuthToken(
        googleId: String,
        oAuthToken: String,
    ) = User
        .update (
            {
                User.googleId eq googleId
            }
        ) {
            it[this.oAuthToken] = oAuthToken
        }


    private fun delete(
        googleId: String,
    ) = User
        .deleteWhere {
            this
                .googleId
                .eq(googleId)
        }

    private fun readUserByOAuthToken(
        oAuthToken: String,
    ): OutUser? = User
        .select {
            User.oAuthToken eq oAuthToken
        }
        .map {
            OutUser(
                username = it[User.username],
                googleId = it[User.googleId],
                oAuthToken = it[User.oAuthToken],
            )
        }
        .singleOrNull()

    private fun readUserByUsername(
        username: String,
    ): OutUser? = User
        .select {
            User.username eq username
        }
        .map {
            OutUser(
                username = it[User.username],
                googleId = it[User.googleId],
                oAuthToken = it[User.oAuthToken],
            )
        }
        .singleOrNull()

    suspend fun readUserByGoogleId(
        googleId: String,
    ): OutUser? = dbQuery {
        User
            .select {
                User.googleId eq googleId
            }
            .map {
                OutUser(
                    username = it[User.username],
                    googleId = it[User.googleId],
                    oAuthToken = it[User.oAuthToken],
                )
            }
            .singleOrNull()
    }

    private fun readAll() = User
        .selectAll()
        .map { user ->
            OutUser(
                username = user[User.username],
                googleId = user[User.googleId],
                oAuthToken = user[User.oAuthToken],
            )
        }
}