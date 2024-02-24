package com.example.plugins.answer

import com.example.plugins.common.dbQuery
import com.example.plugins.question.QuestionService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class AnswerService(
    database: Database,
) {
    object Answer : Table() {
        val id: Column<Int> = integer("id").autoIncrement()
        val questionId: Column<Int> = reference("questionId", QuestionService.Question.id)
        val answer: Column<String> = varchar("answer", length = 250)
        val goodAnswer: Column<Boolean> = bool("goodAnswer")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            try {
                SchemaUtils
                    .drop(Answer)
                SchemaUtils
                    .create(Answer)
            } catch (e: Exception) {
                println("Answer table already exists")
            }
        }
    }

    suspend fun create(
        inAnswer: InAnswer,
        questionIdValue: Int,
        wrapInDbQuery: Boolean = true,
    ): Int = if (wrapInDbQuery) {
        dbQuery {
            create(
                inAnswer = inAnswer,
                questionIdValue = questionIdValue,
            )
        }
    } else {
        create(
            inAnswer = inAnswer,
            questionIdValue = questionIdValue,
        )
    }

    private fun create(
        inAnswer: InAnswer,
        questionIdValue: Int,
    ): Int = Answer
        .insert {
            it[answer] = inAnswer.answer
            it[goodAnswer] = inAnswer.isGoodAnswer
            it[questionId] = questionIdValue
        }[Answer.id]

    suspend fun read(
        id: Int,
    ): OutAnswer? {
        return dbQuery {
            Answer
                .select {
                    Answer.id eq id
                }
                .map {
                    OutAnswer(
                        answer = it[Answer.answer],
                        isGoodAnswer = it[Answer.goodAnswer],
                        id = it[Answer.id],
                        questionId = it[Answer.questionId],
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun readByQuestionId(
        questionId: Int,
        wrapInDbQuery: Boolean = true,
    ): List<OutAnswer> = if (wrapInDbQuery) {
        dbQuery {
            readByQuestionId(
                questionId = questionId,
            )
        }
    } else {
        readByQuestionId(
            questionId = questionId,
        )
    }

    private fun readByQuestionId(
        questionId: Int,
    ) = Answer
        .select {
            Answer.questionId eq questionId
        }
        .map {
            OutAnswer(
                answer = it[Answer.answer],
                isGoodAnswer = it[Answer.goodAnswer],
                id = it[Answer.id],
                questionId = it[Answer.questionId],
            )
        }
}