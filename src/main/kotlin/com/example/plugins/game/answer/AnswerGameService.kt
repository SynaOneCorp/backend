package com.example.plugins.game.answer

import com.example.plugins.common.dbQuery
import com.example.plugins.game.question.QuestionGameService.QuestionGame
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class AnswerGameService(
    database: Database,
) {

    object AnswerGame : Table() {
        val id: Column<Int> = integer("id").autoIncrement()
        val questionGameId: Column<Int> = reference("questionGameId", QuestionGame.questionGameId)
        val answer: Column<String> = varchar("answer", length = 250)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            try {
                SchemaUtils
                    .drop(AnswerGame)
                SchemaUtils
                    .create(AnswerGame)
            } catch (e: Exception) {
                println("AnswerGame table already exists")
            }
        }
    }

    suspend fun addAnswer(
        questionGameIdValue: Int,
        answer: String,
        wrapInDbQuery: Boolean = true,
    ): List<OutAnswerGame> = if (wrapInDbQuery) {
        dbQuery {
            addAnswer(
                questionGameIdValue = questionGameIdValue,
                answer = answer,
            )
        }
    } else {
        addAnswer(
            questionGameIdValue = questionGameIdValue,
            answer = answer,
        )
    }

    private suspend fun addAnswer(
        questionGameIdValue: Int,
        answer: String,
    ) = run {
        AnswerGame
            .insert {
                it[this.questionGameId] = questionGameIdValue
                it[this.answer] = answer
            }[AnswerGame.id]
        readAnswers(
            questionGameIdValue = questionGameIdValue,
            wrapInDbQuery = false,
        )
    }

    suspend fun replaceAnswer(
        questionGameIdValue: Int,
        answerQuestionGameId: Int,
        answer: String,
    ): List<OutAnswerGame> = dbQuery {
        AnswerGame
            .update (
                {
                    AnswerGame.id eq answerQuestionGameId
                }
            ) {
                it[this.answer] = answer
            }

        readAnswers(
            questionGameIdValue = questionGameIdValue,
            wrapInDbQuery = false,
        )
    }

    suspend fun readAnswers(
        questionGameIdValue: Int,
        wrapInDbQuery: Boolean = true,
    ): List<OutAnswerGame> = if (wrapInDbQuery) {
        dbQuery {
            readAnswers(
                questionGameIdValue = questionGameIdValue,
            )
        }
    } else {
        readAnswers(
            questionGameIdValue = questionGameIdValue,
        )
    }

    private fun readAnswers(
        questionGameIdValue: Int,
    ): List<OutAnswerGame> = AnswerGame
        .select {
            AnswerGame.questionGameId eq questionGameIdValue
        }
        .map { databaseAnswer ->
            OutAnswerGame(
                id = databaseAnswer[AnswerGame.id],
                answer = databaseAnswer[AnswerGame.answer],
                questionGameId = databaseAnswer[AnswerGame.questionGameId],
            )
        }
}