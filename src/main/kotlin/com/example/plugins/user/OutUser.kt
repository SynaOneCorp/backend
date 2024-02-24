package com.example.plugins.user

import kotlinx.serialization.Serializable

@Serializable
data class OutUser(
    val username: String,
    val googleId: String,
    val oAuthToken: String,
)