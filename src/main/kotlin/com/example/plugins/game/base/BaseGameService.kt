package com.example.plugins.game.base

import com.example.plugins.UserService
import com.example.plugins.common.DifficultyLevel
import com.example.plugins.common.Language
import com.example.plugins.common.dbQuery
import com.example.plugins.game.answer.AnswerGameService
import com.example.plugins.game.question.OutQuestionGame
import com.example.plugins.game.question.QuestionGameService.QuestionGame
import com.example.plugins.question.QuestionService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class BaseGameService(
    database: Database,
    private val questionService: QuestionService,
    private val answerGameService: AnswerGameService,
) {
    object BaseGame : Table() {
        val baseGameId: Column<Int> = integer("id").autoIncrement()
        val difficultyLevel = enumeration("difficultyLevel", DifficultyLevel::class)
        val language = enumeration("language", Language::class)
        val maxAnswers: Column<Int> = integer("maxAnswers")
        val maxQuestions: Column<Int> = integer("maxQuestions")
        val minAnswerWithoutPoints: Column<Int> = integer("minAnswerWithoutPoints")
        val ownerId: Column<Int> = reference("ownerId", UserService.Users.id)
        val timeToAnswerInSeconds: Column<Int> = integer("timeToAnswerInSeconds")

        override val primaryKey = PrimaryKey(baseGameId)
    }

    init {
        transaction(
            db = database,
        ) {
            try {
                SchemaUtils
                    .drop(BaseGame)
                SchemaUtils
                    .create(BaseGame)
            } catch (e: Exception) {
                println("BaseGame table already exists")
            }
        }
    }

    suspend fun create(
        exposedGame: InBaseGame,
    ): Int = dbQuery {
        BaseGame
            .insert {
                it[ownerId] = exposedGame.ownerId
                it[language] = exposedGame.language
                it[difficultyLevel] = exposedGame.difficultyLevel
                it[maxAnswers] = exposedGame.maxAnswers
                it[maxQuestions] = exposedGame.maxQuestions
                it[timeToAnswerInSeconds] = exposedGame.timeToAnswerInSeconds
                it[minAnswerWithoutPoints] = exposedGame.minAnswerWithoutPoints
            }[BaseGame.baseGameId]
    }

    suspend fun retrieveGame(
        gameId: Int,
    ): OutBaseGame? = dbQuery {
        BaseGame
            .select {
                BaseGame.baseGameId eq gameId
            }
            .map { databaseBaseGame ->
                val questions = QuestionGame
                    .select {
                        QuestionGame.gameId eq gameId
                    }
                    .map { databaseQuestionGame ->
                        val questionGameId = databaseQuestionGame[QuestionGame.questionGameId]
                        val questionId = databaseQuestionGame[QuestionGame.questionId]

                        val questionInfo = questionService
                            .read(
                                id = questionId,
                                wrapInDbQuery = false,
                            )
                            ?: throw NotImplementedError()

                        val answers = answerGameService
                            .readAnswers(
                                questionGameIdValue = questionGameId,
                                wrapInDbQuery = false,
                            )

                        OutQuestionGame(
                            answers = answers,
                            status = databaseQuestionGame[QuestionGame.status],
                            question = questionInfo,
                            questionGameId = questionGameId,
                            questionNumber = databaseQuestionGame[QuestionGame.questionNumber],
                        )
                    }

                OutBaseGame(
                    id = gameId,
                    language = databaseBaseGame[BaseGame.language],
                    ownerId = databaseBaseGame[BaseGame.ownerId],
                    difficultyLevel = databaseBaseGame[BaseGame.difficultyLevel],
                    maxAnswers = databaseBaseGame[BaseGame.maxAnswers],
                    maxQuestions = databaseBaseGame[BaseGame.maxQuestions],
                    minAnswerWithoutPoints = databaseBaseGame[BaseGame.minAnswerWithoutPoints],
                    timeToAnswerInSeconds = databaseBaseGame[BaseGame.timeToAnswerInSeconds],
                    questions = questions,
                )
            }
            .singleOrNull()
    }

}
