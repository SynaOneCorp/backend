package com.example.plugins.game.answer

import kotlinx.serialization.Serializable

@Serializable
data class OutAnswerGame(
    val id: Int,
    val answer: String,
    val questionGameId: Int,
    val bet: Int = 0,
)