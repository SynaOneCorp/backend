package com.example.plugins

import com.example.plugins.common.dbQuery
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*

@Serializable
data class ExposedUser(
    val username: String,
    val googleId: String,
    val token: String?,
)
class UserService(private val database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val username = varchar("username", length = 50)
        val googleId = varchar("googleId", length = 50)
        val token = varchar("token", length = 250).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            try {
                //SchemaUtils.drop(Users)
                SchemaUtils.create(Users)
            } catch (e: Exception) {
                println("Users table already exists")
            }
        }
    }

    suspend fun create(user: ExposedUser): Int = dbQuery {
        Users.insert {
            it[username] = user.username
            it[googleId] = user.googleId
            it[token] = user.token
        }[Users.id]
    }

    suspend fun readById(id: Int): ExposedUser? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map {
                    ExposedUser(
                        it[Users.username],
                        it[Users.googleId],
                        it[Users.token],
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun readAll(): List<ExposedUser> {
        return dbQuery {
            Users
                .selectAll()
                .map {
                    ExposedUser(
                        it[Users.username],
                        it[Users.googleId],
                        it[Users.token],
                    )
                }
        }
    }

    suspend fun readByUsername(
        username: String,
    ): ExposedUser? {
        return dbQuery {
            Users.select { Users.username eq username }
                .map {
                    ExposedUser(
                        it[Users.username],
                        it[Users.googleId],
                        it[Users.token],
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun readByAccessToken(
        accessToken: String,
    ): ExposedUser? {
        return dbQuery {
            Users.select { Users.token eq accessToken }
                .map {
                    ExposedUser(
                        it[Users.username],
                        it[Users.googleId],
                        it[Users.token],
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: ExposedUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[username] = user.username
                it[googleId] = user.googleId
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }
}
