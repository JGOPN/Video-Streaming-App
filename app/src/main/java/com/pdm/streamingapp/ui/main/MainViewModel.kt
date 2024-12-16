package com.pdm.streamingapp.ui.main

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pdm.streamingapp.data.AppDatabase
import com.pdm.streamingapp.model.Movie
import com.pdm.streamingapp.model.MovieEntity
import com.pdm.streamingapp.network.StreamingAppAPI
import com.pdm.streamingapp.network.tmdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


enum class MainScreens(val title : String){
    Home("Home"),
    Search("Search movies"),
    Settings("Settings")
}

data class MainUiState(
    val currentScreen : MainScreens = MainScreens.Home,
    val expandedCardId : Int = -1,  //id for the selected movie
    val movieList : List<Movie> = listOf()
)

data class SearchUiState(
    val searchQuery : String = "",
    val showingList: Boolean = false,
    val expandedCardId : Int = -1,  //id for the selected movie
    val movieList : List<Movie> = listOf() //stores the list filtered by query
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    //AndroidViewModel is deprecated but we use it anyways because its the easiest way to get application context to start db

    private val database = AppDatabase.getDatabase(application)
    private val movieDao = database.movieDao()

    init {
        getMovieList()
    }

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    fun setCurrentScreen(screen: MainScreens){
        _mainUiState.update {
                currentState -> currentState.copy(currentScreen = screen)
        }
    }

    fun toggleCardExpansion(cardId: Int) {
        _mainUiState.update {
                currentState -> currentState.copy(expandedCardId = cardId)
        }
    }

    fun getMovieList() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { StreamingAppAPI.retrofitService.getMovies() }
            }.onSuccess { movieList ->
                _mainUiState.update { currentState ->
                    currentState.copy(movieList = movieList)
                }
            }.onFailure { exception ->
                handleExceptions(exception)
            }
        }
    }

    fun fetchThumbnail(movieTitle: String, onSuccess: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = tmdbApi.searchMovies(movieTitle)
                val posterPath = response.results.firstOrNull()?.posterPath
                onSuccess(posterPath?.let { "https://image.tmdb.org/t/p/w500$it"})
            } catch (e: Exception) {
                handleExceptions(e)
            }
        }
    }

    fun signOut(){
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
            } catch (e: Exception) {
                handleExceptions(e)
            }
        }
    }

    suspend fun saveMovieLocally(context: Context, movieId: Int, responseBody: ResponseBody, fileName: String): File? {
        //this function receives the response from downloadMovie and saves it locally
        return withContext(Dispatchers.IO) {
            try {
                val outputStream: OutputStream
                val file: File

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Scoped Storage: Use MediaStore for Android 10+
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                        ?: throw Exception("Failed to create MediaStore entry.")

                    Log.d("MainActivity","Saving movie locally @uri:  $uri")

                    outputStream = context.contentResolver.openOutputStream(uri)
                        ?: throw Exception("Failed to open output stream.")

                    movieDao.insertMovie(MovieEntity(id = 0, movieId = movieId, uri = uri.toString(), filePath = null))

                    file = File(Environment.DIRECTORY_MOVIES, fileName)
                } else {
                    // Legacy file handling for Android 9 and below
                    val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    file = File(moviesDir, fileName)
                    file.parentFile?.mkdirs() // Ensure directories exist
                    movieDao.insertMovie(MovieEntity(id = 0, movieId = movieId, uri = "", filePath = "$moviesDir/$fileName"))
                    outputStream = FileOutputStream(file)
                }

                // Write the data to the file
                responseBody.byteStream().use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                file // Return the file reference after saving
            } catch (e: Exception) {
                e.printStackTrace()
                null // Return null on error
            }
        }
    }

    fun downloadAndSaveMovie(context: Context, movieId: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val responseBody: ResponseBody = StreamingAppAPI.retrofitService.downloadMovie(movieId = movieId.toString())

                val fileName = "movie_$movieId.mp4"

                val savedFile: File? = saveMovieLocally(context, movieId, responseBody, fileName)

                if (savedFile != null) {
                    onResult(true, "Movie saved at: ${savedFile.absolutePath}")
                } else {
                    onResult(false, "Failed to save the movie.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    suspend fun deleteMovie(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            Log.d("MainActivity","Called deleteMovie with uri: $uri")
            try {
                // Attempt to delete the file via ContentResolver
                val rowsDeleted = context.contentResolver.delete(uri, null, null)
                rowsDeleted > 0 // Return true if at least one row was deleted
            } catch (e: Exception) {
                e.printStackTrace()
                false // Return false on error
            }
        }
    }

    fun deleteMovieCall(context: Context, uri: Uri, movieId: Int){
        viewModelScope.launch {
            deleteMovie(context, uri)
            movieDao.deleteMovie(movieId)
        }
    }

    fun getDownloadPath(movieId: Int, pathCallback: (String?) -> Unit) {
        //checks if a movie is downloaded locally (thus in the room db)
        viewModelScope.launch {
            val movie = movieDao.getMovieById(movieId)
            if (movie?.filePath != null) {
                pathCallback(movie.filePath)
            } else {
                pathCallback(null)
            }
        }
    }

    fun getDownloadUri(movieId: Int, pathCallback: (Uri?) -> Unit) {
        //checks if a movie is downloaded locally (thus in the room db)
        viewModelScope.launch {
            val movie = movieDao.getMovieById(movieId)
            if (movie?.uri != null) {
                pathCallback(Uri.parse(movie.uri))
            } else {
                pathCallback(null)
            }
        }
    }


    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    fun updateSearchQuery(query: String){
        _searchUiState.update {
                currentState -> currentState.copy(searchQuery = query)
        }
    }

    fun toggleShowingList(b: Boolean = false) {
        _searchUiState.update {
                currentState -> currentState.copy(showingList = b)
        }
    }

    fun toggleSearchCardExpansion(cardId: Int) {
        _searchUiState.update {
                currentState -> currentState.copy(expandedCardId = cardId)
        }
    }

    fun filterMovieList(){
        //takes the movielist from main screen, filters for title matching query
        val movieListFiltered = _mainUiState.value.movieList.filter{ it.title.contains(_searchUiState.value.searchQuery, ignoreCase = true) }
        _searchUiState.update {
                currentState -> currentState.copy(movieList = movieListFiltered)
        }
    }
}

fun handleExceptions(exception: Throwable){
    when (exception) {
        is IOException -> Log.e("Network", "Network error: ${exception.message}")
        is HttpException -> Log.e("Network", "HTTP error: ${exception.message}")
        else -> Log.e("Network", "Unexpected error: ${exception.message}")
    }
}
