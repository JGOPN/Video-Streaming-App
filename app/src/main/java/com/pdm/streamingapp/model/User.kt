package com.pdm.streamingapp.model

import java.time.LocalDate

data class User(
    val id : Int,
    val username : String,
    val password : String,
    val email : String,
    val isAdmin : Boolean,
    val birthdate: LocalDate
)