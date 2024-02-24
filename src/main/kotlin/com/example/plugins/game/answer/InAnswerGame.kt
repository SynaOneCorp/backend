package com.example.plugins.game.answer

import kotlinx.serialization.Serializable

@Serializable
data class InAnswerGame(
    val answer: String,
)