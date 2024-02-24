package com.example.plugins.question

import kotlinx.serialization.Serializable

data class Question(
    val id: Int,
    val language: Language,
    val question: String,
    val answer: String,
    val difficultyLevel: DifficultyLevel,
) {
    @Serializable
    enum class Language {
        French,
        English,
    }

    @Serializable
    enum class DifficultyLevel {
        Easy,
        Medium,
        Hard,
    }
}