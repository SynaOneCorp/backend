package com.example.plugins.answer

import kotlinx.serialization.Serializable

@Serializable
data class InAnswer(
    val answer: String,
    val isGoodAnswer: Boolean,
)