package com.pdm.streamingapp.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

//defines a separate movie class for use with Room.
// It is essentially used to keep track of which movies have been downloaded and where they are on the device

@Entity(tableName = "downloaded_movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val movieId: Int,
    val uri: String,
    val filePath: String?
)

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("SELECT * FROM downloaded_movies WHERE movieId = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query("DELETE FROM downloaded_movies WHERE movieId = :movieId")
    suspend fun deleteMovie(movieId: Int)
}