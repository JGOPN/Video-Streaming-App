package com.pdm.streamingapp.model

data class Movie(
    val id : Int,
    val title : String,
    val description : String,
    val releaseYear : Int,
    val submittedBy : Int,
    val duration : Int,
    val genres : List<String>
    )