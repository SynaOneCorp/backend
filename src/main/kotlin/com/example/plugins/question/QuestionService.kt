package com.example.plugins.question

import com.example.plugins.answer.AnswerService
import com.example.plugins.answer.AnswerService.Answer
import com.example.plugins.common.DifficultyLevel
import com.example.plugins.common.Language
import com.example.plugins.common.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class QuestionService(
    database: Database,
    private val answerService: AnswerService,
) {
    object Question : Table() {
        val id: Column<Int> = integer("id").autoIncrement()
        val question: Column<String> = varchar("question", length = 250)
        val difficultyLevel = enumeration("difficultyLevel", DifficultyLevel::class)
        val language = enumeration("language", Language::class)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            try {
                SchemaUtils.dropDatabase()
                SchemaUtils
                    .drop(Question)
                SchemaUtils
                    .create(Question)
            } catch (e: Exception) {
                println("Question table already exists")
            }
        }
    }

    suspend fun create(
        inQuestion: InQuestion,
    ): Int = dbQuery {
        val questionId = Question
            .insert {
                it[this.question] = inQuestion.question
                it[this.language] = inQuestion.language
                it[this.difficultyLevel] = inQuestion.difficultyLevel
            }[Question.id]
        inQuestion
            .answers
            .forEach { answer ->
                answerService
                    .create(
                        inAnswer = answer,
                        questionIdValue = questionId,
                        wrapInDbQuery = false,
                    )
            }
        questionId
    }

    suspend fun readRandom(
        wrapInDbQuery: Boolean = true,
    ): OutQuestion = if (wrapInDbQuery) {
        dbQuery {
            readRandom()
        }
    } else {
        readRandom()
    }

    private suspend fun readRandom() = Question
        .selectAll()
        .shuffled()
        .first()
        .run {
            val questionId = this[Question.id]
            val answers = answerService
                .readByQuestionId(
                    questionId = questionId
                )
            OutQuestion(
                question = this[Question.question],
                language = this[Question.language],
                difficultyLevel = this[Question.difficultyLevel],
                id = questionId,
                answers = answers,
            )
        }

    suspend fun read(
        id: Int,
        wrapInDbQuery: Boolean = true,
    ): OutQuestion? = if (wrapInDbQuery) {
        dbQuery {
            read(
                id = id,
            )
        }
    } else {
        read(
            id = id,
        )
    }

    suspend fun read(
        id: Int,
    ): OutQuestion? = Question
        .select {
            Question.id eq id
        }
        .map {
            val answers = answerService
                .readByQuestionId(
                    questionId = id,
                    wrapInDbQuery = false,
                )
            OutQuestion(
                question = it[Question.question],
                language = it[Question.language],
                difficultyLevel = it[Question.difficultyLevel],
                id = it[Question.id],
                answers = answers,
            )
        }
        .singleOrNull()

    suspend fun read(
        questionToFind: String,
    ): OutQuestion? {
        return dbQuery {
            Question
                .select {
                    Question.question eq questionToFind
                }
                .map {
                    val questionId = it[Question.id]
                    val answers = answerService
                        .readByQuestionId(
                            questionId = questionId
                        )
                    OutQuestion(
                        question = it[Question.question],
                        language = it[Question.language],
                        difficultyLevel = it[Question.difficultyLevel],
                        id = questionId,
                        answers = answers,
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun readAll(): List<OutQuestion> {
        return dbQuery {
            val questions = Question
                .selectAll()
                .map { question ->
                    val questionId = question[Question.id]

                    val answers = answerService
                        .readByQuestionId(
                            questionId = questionId
                        )

                    OutQuestion(
                        question = question[Question.question],
                        language = question[Question.language],
                        difficultyLevel = question[Question.difficultyLevel],
                        id = questionId,
                        answers = answers,
                    )
                }
            questions
        }
    }

    suspend fun delete(
        id: Int,
    ) {
        dbQuery {
            Answer
                .deleteWhere {
                    this
                        .questionId
                        .eq(id)
                }
            Question
                .deleteWhere {
                    this
                        .id
                        .eq(id)
                }
        }
    }

    suspend fun update(
        id: Int,
        inQuestion: InQuestion,
    ) {
        dbQuery {
            Question
                .update(
                    {
                        Question.id eq id
                    }
                ) {
                    it[question] = inQuestion.question
                    it[language] = inQuestion.language
                    it[difficultyLevel] = inQuestion.difficultyLevel
            }
        }
    }
}