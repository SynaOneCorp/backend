package com.example.plugins.game.question

import com.example.plugins.game.answer.OutAnswerGame
import com.example.plugins.game.base.OutBaseGame
import com.example.plugins.question.OutQuestion
import kotlinx.serialization.Serializable

@Serializable
data class OutQuestionGame(
    val answers: List<OutAnswerGame>,
    val status: OutBaseGame.Status,
    val question: OutQuestion,
    val questionGameId: Int,
    val questionNumber: Int,
)
