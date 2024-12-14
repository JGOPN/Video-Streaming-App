package com.pdm.streamingapp.model

import kotlinx.datetime.LocalDate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("user_id")
    val id : Int,
    val username : String,
    val email : String,
    val birthdate: LocalDate,
    @SerialName("is_admin")
    val isAdmin : Boolean
)