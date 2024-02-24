package com.example.plugins.answer

import kotlinx.serialization.Serializable

@Serializable
data class OutAnswer(
    val answer: String,
    val isGoodAnswer: Boolean,
    val id: Int,
    val questionId: Int,
)