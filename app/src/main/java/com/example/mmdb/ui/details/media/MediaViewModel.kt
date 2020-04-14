package com.example.mmdb.ui.details.media

import android.app.Application
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.example.mmdb.MovieDetailsArgs
import com.example.mmdb.R
import com.example.mmdb.model.data.Images
import com.example.mmdb.model.data.Video
import com.example.mmdb.model.remote.repositories.RemoteMovieRepository
import com.example.mmdb.util.KEY_TRAILER_TYPE
import com.example.mmdb.util.KEY_YOUTUBE_SITE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaViewModel(application: Application, arguments: Bundle?) : AndroidViewModel(application) {
    private val _trailer = MutableLiveData<Video>()
    private var _images = MutableLiveData<Images>()
    private val _loading = MutableLiveData<Boolean>()

    val trailer: LiveData<Video> = _trailer
    var images: LiveData<Images>? = _images
    val loading: LiveData<Boolean> = _loading

    private lateinit var safeArgs: MovieDetailsArgs

    init {
        _loading.value = true
        arguments?.let {
            safeArgs = MovieDetailsArgs.fromBundle(it)
            if (safeArgs.movieLocalId == 0){
                getImagesRemote(safeArgs.movieRemoteId)
                getTrailer(safeArgs.movieRemoteId)
            } else {
                getImagesLocal(safeArgs.movieLocalId)
            }
        }
    }

    private fun getTrailer(movieId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            val response = RemoteMovieRepository()
                .getVideo(movieId)
            withContext(Dispatchers.Main){
                _trailer.value = filterVideos(response.body()!!.videoList)
                _loading.value = false
            }
        }
    }

    private fun filterVideos(videoList: List<Video>): Video? {
        for (video in videoList){
            if (video.siteType == KEY_YOUTUBE_SITE && video.videoType == KEY_TRAILER_TYPE)
                return video
        }
        return null
    }

    private fun getImagesRemote(movieId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RemoteMovieRepository()
                .getImages(movieId)
            withContext(Dispatchers.Main) {
                _images.value = Images(0, response.body()!!.posterList, response.body()!!.backdropList)
                _loading.value = false
            }
        }
    }

    private fun getImagesLocal(movieId: Int) {
        images = com.example.mmdb.model.room.repositories.RoomMovieRepository(
            getApplication()
        ).getImagesById(movieId)
        _loading.value = false
    }

    fun onNavigationItemSelected(view: View, menuItem: MenuItem): Boolean{
        when (menuItem.itemId) {
            R.id.overview_menu_item -> {
                val action = MediaFragmentDirections.actionMovieOverview()
                action.movieRemoteId = safeArgs.movieRemoteId
                action.movieLocalId = safeArgs.movieLocalId
                Navigation.findNavController(view).navigate(action)
            }
            R.id.cast_menu_item -> {
                val action = MediaFragmentDirections.actionMovieCast()
                action.movieRemoteId = safeArgs.movieRemoteId
                action.movieLocalId = safeArgs.movieLocalId
                Navigation.findNavController(view).navigate(action)
            }
        }
        return true
    }
}