package com.example.plugins.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    val id: String,
    val name: String? = null,
    @SerialName("given_name")
    val givenName: String? = null,
)