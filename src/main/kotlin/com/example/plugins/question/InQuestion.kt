package com.example.plugins.question

import com.example.plugins.answer.InAnswer
import com.example.plugins.common.DifficultyLevel
import com.example.plugins.common.Language
import kotlinx.serialization.Serializable

@Serializable
data class InQuestion(
    val question: String,
    val difficultyLevel: DifficultyLevel,
    val language: Language,
    val answers: List<InAnswer> = emptyList(),
)