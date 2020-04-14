package com.example.mmdb.model.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mmdb.model.data.Movie

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateMovie(movie: Movie): Long

    @Query("SELECT * FROM movie WHERE roomId = :movieId")
    fun getMovieLiveDataById(movieId: Int): LiveData<Movie>

    @Query("SELECT * FROM movie WHERE roomId = :movieId")
    fun getMovieById(movieId: Int): Movie

    @Query("SELECT * FROM movie WHERE roomId IN (:movieIdList)")
    fun getMoviesFromIdList(movieIdList: List<Int>): List<Movie>

    @Query("SELECT * FROM movie")
    fun getAllMovies(): LiveData<List<Movie>>

    @Query("DELETE FROM movie WHERE roomId = :movieId")
    fun deleteMovieById(movieId: Int)

    @Delete
    fun deleteMovie(movie: Movie)
}