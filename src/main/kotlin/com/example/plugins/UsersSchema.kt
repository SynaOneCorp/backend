package com.example.plugins

import com.example.plugins.common.dbQuery
import com.example.plugins.question.Question
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

@Serializable
data class ExposedUser(val username: String, val googleId: String)
class UserService(private val database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val username = varchar("username", length = 50)
        val googleId = varchar("googleId", length = 50)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            try {
                SchemaUtils.drop(Users)
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
        }[Users.id]
    }

    suspend fun read(id: Int): ExposedUser? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { ExposedUser(it[Users.username], it[Users.googleId]) }
                .singleOrNull()
        }
    }

    suspend fun read(username: String): ExposedUser? {
        return dbQuery {
            Users.select { Users.username eq username }
                .map {
                    println("TEST12 => $it")
                    ExposedUser(it[Users.username], it[Users.googleId])
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
