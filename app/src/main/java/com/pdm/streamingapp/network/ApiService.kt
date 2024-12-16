package com.pdm.streamingapp.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pdm.streamingapp.model.Movie
import com.pdm.streamingapp.model.User
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val BASE_URL =
        //"http://192.168.1.72:8080" //local machine ip
        "http://35.204.253.240:8080/" //remote ip

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL).build()

//The converter tells Retrofit what to do with the data it gets back from the web service. In this case, you want Retrofit to fetch a
// JSON response from the web service and return it as a String. Retrofit has a ScalarsConverter that supports strings and other primitive types.

interface ApiService {
    @GET("movies")
    suspend fun getMovies(): List<Movie>

    @GET("movies/download")
    suspend fun downloadMovie(
        @Query("movieId") movieId: String
    ): ResponseBody

    @POST("users")
    suspend fun addUser(@Body user: User): Response<Unit>
    
    @GET("genres")
    suspend fun getGenres(): List<String>

}

// A public Api object that exposes the lazy-initialized Retrofit service
object StreamingAppAPI {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}