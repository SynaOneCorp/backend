package com.example.plugins.question

import com.example.plugins.answer.OutAnswer
import com.example.plugins.common.DifficultyLevel
import com.example.plugins.common.Language
import kotlinx.serialization.Serializable

@Serializable
data class OutQuestion(
    val question: String,
    val language: Language,
    val difficultyLevel: DifficultyLevel,
    val id: Int,
    val answers: List<OutAnswer>,
)