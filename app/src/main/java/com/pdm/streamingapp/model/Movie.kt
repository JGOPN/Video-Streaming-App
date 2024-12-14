package com.pdm.streamingapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    @SerialName("movie_id")
    val id : Int,
    val title : String,
    val description : String,
    @SerialName("release_year")
    val releaseYear : Int,
    @SerialName("submitted_by")
    val submittedBy : Int?,
    val duration : Int,
    val genres : List<String>,
    val movieUrl : String? = null //instead of sending a file, the user can send a url to a movie and the backend will download it.
    )