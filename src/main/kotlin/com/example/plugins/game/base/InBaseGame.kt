package com.example.plugins.game.base

import com.example.plugins.common.DifficultyLevel
import com.example.plugins.common.Language
import kotlinx.serialization.Serializable

@Serializable
data class InBaseGame(
    val ownerId: Int,
    val language: Language = Language.French,
    val difficultyLevel: DifficultyLevel = DifficultyLevel.Easy,
    val maxAnswers: Int = 3,
    val maxQuestions: Int = 0,
    val timeToAnswerInSeconds: Int = 60,
    val minAnswerWithoutPoints: Int = 0,
)