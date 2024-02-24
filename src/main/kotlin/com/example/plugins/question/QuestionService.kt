package com.example.plugins.question

import com.example.plugins.common.dbQuery
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class QuestionService(
    private val database: Database,
) {
    object Questions : Table() {
        val id: Column<Int> = integer("id").autoIncrement()
        val question: Column<String> = varchar("question", length = 50)
        val answer: Column<String> = varchar("answer", length = 50)
        val difficultyLevel = enumeration("difficultyLevel", Question.DifficultyLevel::class)
        val language = enumeration("language", Question.Language::class)

        override val primaryKey = PrimaryKey(id)
    }

    @Serializable
    data class ExposedQuestion(
        val question: String,
        val answer: String,
        val language: Question.Language,
        val difficultyLevel: Question.DifficultyLevel,
    )

    init {
        transaction(database) {
            try {
                SchemaUtils.drop(Questions)
                SchemaUtils.create(Questions)
            } catch (e: Exception) {
                println("Questions table already exists")
            }
        }
    }

    suspend fun create(
        exposedQuestion: ExposedQuestion,
    ): Int = dbQuery {
        Questions
            .insert {
                it[question] = exposedQuestion.question
                it[answer] = exposedQuestion.answer
                it[language] = exposedQuestion.language
                it[difficultyLevel] = exposedQuestion.difficultyLevel
            }[Questions.id]
    }

    suspend fun readRandom(): ExposedQuestion =
        dbQuery {
            Questions
                .selectAll()
                .shuffled()
                .first()
                .run {
                    ExposedQuestion(
                        question = this[Questions.question],
                        answer = this[Questions.answer],
                        language = this[Questions.language],
                        difficultyLevel = this[Questions.difficultyLevel],
                    )
                }
        }

    suspend fun read(
        id: Int,
    ): ExposedQuestion? {
        return dbQuery {
            Questions
                .select {
                    Questions.id eq id
                }
                .map {
                    ExposedQuestion(
                        question = it[Questions.question],
                        answer = it[Questions.answer],
                        language = it[Questions.language],
                        difficultyLevel = it[Questions.difficultyLevel],
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun read(
        questionToFind: String,
    ): ExposedQuestion? {
        return dbQuery {
            Questions
                .select {
                    Questions.question eq questionToFind
                }
                .map {
                    ExposedQuestion(
                        question = it[Questions.question],
                        answer = it[Questions.answer],
                        language = it[Questions.language],
                        difficultyLevel = it[Questions.difficultyLevel],
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun delete(
        id: Int,
    ) {
        dbQuery {
            Questions.deleteWhere {
                Questions
                    .id
                    .eq(id)
            }
        }
    }

    suspend fun update(
        id: Int,
        exposedQuestion: ExposedQuestion,
    ) {
        dbQuery {
            Questions
                .update(
                    {
                        Questions.id eq id
                    }
                ) {
                    it[question] = exposedQuestion.question
                    it[answer] = exposedQuestion.answer
                    it[language] = exposedQuestion.language
                    it[difficultyLevel] = exposedQuestion.difficultyLevel
            }
        }
    }
}