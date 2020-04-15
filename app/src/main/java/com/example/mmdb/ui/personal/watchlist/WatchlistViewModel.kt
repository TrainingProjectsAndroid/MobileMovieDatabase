package com.example.mmdb.ui.personal.watchlist

import android.app.Application
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.example.mmdb.NavGraphDirections
import com.example.mmdb.R
import com.example.mmdb.model.data.*
import com.example.mmdb.model.remote.repositories.RemoteMovieRepository
import com.example.mmdb.model.room.databases.MovieListDatabase
import com.example.mmdb.model.room.repositories.MovieListRepository
import com.example.mmdb.model.room.repositories.RoomMovieRepository
import com.example.mmdb.model.room.repositories.WatchlistRepository
import com.example.mmdb.ui.personal.customlists.addtolists.AddToListsPopupWindow
import com.example.mmdb.ui.personal.customlists.addtolists.AddToListsTaskManager
import com.example.mmdb.util.isNetworkAvailable
import com.example.mmdb.util.networkUnavailableNotification
import com.example.mmdb.util.showProgressSnackBar
import com.example.mmdb.util.showToast
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchlistViewModel(application: Application) : AndroidViewModel(application) {

    private val _movies = MutableLiveData<MutableList<Movie>>()
    private val _error = MutableLiveData<Boolean>()
    private val _loading = MutableLiveData<Boolean>()

    var movies: LiveData<MutableList<Movie>> = _movies
    val error: LiveData<Boolean> = _error
    val loading: LiveData<Boolean> = _loading

    private val movieList = mutableListOf<WatchlistMovie>()

    private val remoteMovieRepository = RemoteMovieRepository()
    private val watchlistRepository = WatchlistRepository(application)

    init {
        getWatchlist()
    }

    fun refresh() {
        _movies.value = null
        getWatchlist()
    }

    private fun getWatchlist() {
        _loading.value = true
        _error.value = false
        if (isNetworkAvailable(getApplication())) {
            CoroutineScope(Dispatchers.IO).launch {
                movieList.clear()
                movieList.addAll(watchlistRepository.getAllMovies())
                for (movie in movieList)
                    getMovie(movie.movieId)
                withContext(Dispatchers.Main) { _error.value = movieList.isNullOrEmpty() }
            }
        } else {
            _error.value = true
            networkUnavailableNotification(getApplication())
        }
        _loading.value = false
    }

    private fun getMovie(movieId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = remoteMovieRepository.getMovieDetails(movieId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        it.isInWatchlist = true
                        formatGenres(it)
                    }
                }
            }
        }
    }

    private fun formatGenres(movie: Movie) {
        CoroutineScope(Dispatchers.IO).launch {
            movie.formatGenresString(movie.genres)
            insertMovieToData(movie)
        }
    }

    private fun insertMovieToData(movie: Movie) {
        CoroutineScope(Dispatchers.Main).launch {
            val tmpList = _movies.value
            if (tmpList.isNullOrEmpty())
                _movies.value = mutableListOf(movie)
            else {
                tmpList.add(movie)
                _movies.value = tmpList
            }
        }
    }

    fun updateWatchlist(movie: Movie) {
        if (movie.isInWatchlist) {
            watchlistRepository.insertOrUpdateMovie(WatchlistMovie(movie.remoteId))
            showToast(
                getApplication(),
                getApplication<Application>().getString(R.string.added_to_watchlist),
                Toast.LENGTH_SHORT
            )
        } else {
            watchlistRepository.deleteWatchlistMovie(movie.remoteId)
            showToast(
                getApplication(),
                getApplication<Application>().getString(R.string.deleted_from_watchlist),
                Toast.LENGTH_SHORT
            )
        }
    }

    fun onMovieClicked(view: View, movie: Movie) {
        val action = WatchlistFragmentDirections.actionMovieDetails()
        action.movieRemoteId = movie.remoteId
        Navigation.findNavController(view).navigate(action)
    }

    // ------------ Custom lists ------------//

    fun onPlaylistAddCLicked(movie: Movie, root: View) {
        AddToListsTaskManager(
            getApplication(),
            AddToListsPopupWindow(
                root,
                View.inflate(root.context, R.layout.popup_window_personal_lists_to_add, null),
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                movie
            )
        )
    }
}