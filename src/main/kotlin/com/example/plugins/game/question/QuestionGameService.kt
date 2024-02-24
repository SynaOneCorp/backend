package com.example.plugins.game.question

import com.example.plugins.common.dbQuery
import com.example.plugins.game.answer.AnswerGameService
import com.example.plugins.game.base.BaseGameService.BaseGame
import com.example.plugins.game.base.OutBaseGame
import com.example.plugins.question.QuestionService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class QuestionGameService(
    database: Database,
    private val questionService: QuestionService,
    private val answerGameService: AnswerGameService,
) {
    object QuestionGame : Table() {
        val questionGameId: Column<Int> = integer("id").autoIncrement()

        val gameId: Column<Int> = reference("gameId", BaseGame.baseGameId)
        val questionId: Column<Int> = reference("questionId", QuestionService.Question.id)
        val questionNumber: Column<Int> = integer("questionNumber")
        val status = enumeration("status", OutBaseGame.Status::class)

        override val primaryKey = PrimaryKey(questionGameId)
    }

    init {
        transaction(
            db = database,
        ) {
            try {
                SchemaUtils
                    .drop(QuestionGame)
                SchemaUtils
                    .create(QuestionGame)
            } catch (e: Exception) {
                println("QuestionGame table already exists")
            }
        }
    }

    suspend fun newQuestion(
        gameIdValue: Int,
    ): OutQuestionGame = dbQuery {
        val randomQuestion = questionService
            .readRandom(
                wrapInDbQuery = false,
            )
        val questionNumberValue = currentQuestionNumber(
            gameId = gameIdValue,
            wrapInDbQuery = false
        ) + 1

        val questionGameId = QuestionGame
            .insert {
                it[questionId] = randomQuestion.id
                it[questionNumber] = questionNumberValue
                it[status] = OutBaseGame.Status.Pending
                it[gameId] = gameIdValue
            }[QuestionGame.questionGameId]

        answerGameService
            .addAnswer(
                questionGameIdValue = questionGameId,
                answer = randomQuestion
                    .answers
                    .first { it.isGoodAnswer }
                    .answer,
                wrapInDbQuery = false,
            )

        getQuestion(
            questionGameId = questionGameId,
            wrapInDbQuery = false,
        )
    }

    private suspend fun getQuestion(
        questionGameId: Int,
        wrapInDbQuery: Boolean = true,
    ): OutQuestionGame = if (wrapInDbQuery) {
        dbQuery {
            getQuestion(
                questionGameId = questionGameId,
            )
        }
    } else {
        getQuestion(
            questionGameId = questionGameId,
        )
    }

    private suspend fun getQuestion(
        questionGameId: Int,
    ): OutQuestionGame = run {
        QuestionGame
            .select {
                QuestionGame.questionGameId eq questionGameId
            }
            .map { databaseQuestionGame ->
                val gameId = databaseQuestionGame[QuestionGame.gameId]
                val questionId = databaseQuestionGame[QuestionGame.questionId]
                val questionNumber = databaseQuestionGame[QuestionGame.questionNumber]
                val status = databaseQuestionGame[QuestionGame.status]

                questionService
                    .read(
                        id = questionId,
                        wrapInDbQuery = false,
                    )
                    ?.let { outQuestion ->
                        OutQuestionGame(
                            question = outQuestion,
                            questionGameId = questionGameId,
                            answers = emptyList(),
                            status = status,
                            questionNumber = questionNumber,
                        )
                    }
                    ?: throw NotImplementedError()
            }
            .single()
    }

    private suspend fun currentQuestionNumber(
        gameId: Int,
        wrapInDbQuery: Boolean = true,
    ): Int = if (wrapInDbQuery) {
        dbQuery {
            currentQuestionNumber(
                gameId = gameId,
            )
        }
    } else {
        currentQuestionNumber(
            gameId = gameId,
        )
    }

    private fun currentQuestionNumber(
        gameId: Int,
    ): Int = QuestionGame
        .select {
            QuestionGame.gameId eq gameId
        }
        .count()
        .toInt()

}