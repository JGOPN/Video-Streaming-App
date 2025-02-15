package com.pdm.streamingapp.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

private val json = Json {
    ignoreUnknownKeys = true // Ignore fields not in data class
}

val tmdbApi: TMDBApiService = Retrofit.Builder()
    .baseUrl("https://api.themoviedb.org/3/")
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(TMDBApiService::class.java)

// api key from local.properties
private val tmdbApiKey: String = BuildConfig.TMDB_API_KEY

interface TMDBApiService {
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = tmdbApiKey
    ): TmdbResponse
}

@Serializable
data class TmdbResponse(
    val results: List<TmdbMovie>
)

@Serializable
data class TmdbMovie(
    val title: String,
    @SerialName("poster_path") val posterPath: String?
)
