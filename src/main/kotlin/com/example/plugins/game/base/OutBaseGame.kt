package com.example.plugins.game.base

import com.example.plugins.common.DifficultyLevel
import com.example.plugins.common.Language
import com.example.plugins.game.question.OutQuestionGame
import kotlinx.serialization.Serializable

@Serializable
data class OutBaseGame(
    val id: Int,
    val language: Language,
    val ownerId: Int,
    val difficultyLevel: DifficultyLevel,
    val maxAnswers: Int,
    val maxQuestions: Int,
    val minAnswerWithoutPoints: Int,
    val timeToAnswerInSeconds: Int,
    val questions: List<OutQuestionGame>,
) {
    @Serializable
    enum class Status {
        Created,
        Pending,
        Answered,
    }
}