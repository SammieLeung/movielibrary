package com.hphtv.movielibrary.ui.homepage.fragment.filter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hphtv.movielibrary.data.Config
import com.hphtv.movielibrary.data.Constants.VideoType
import com.hphtv.movielibrary.data.pagination.PaginatedDataLoader
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase
import com.hphtv.movielibrary.roomdb.dao.MovieDao
import com.hphtv.movielibrary.roomdb.dao.VideoTagDao
import com.hphtv.movielibrary.roomdb.entity.Shortcut
import com.hphtv.movielibrary.roomdb.entity.VideoTag
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView
import com.hphtv.movielibrary.util.ScraperSourceTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterViewModel(application: Application) : AndroidViewModel(application) {
    private val movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication())
    private val movieDao: MovieDao = movieLibraryRoomDatabase.movieDao
    private val videoTagDao: VideoTagDao = movieLibraryRoomDatabase.videoTagDao
    private var shortcut: Shortcut? = null
    private var videoTag: VideoTag? = null
    private var genreName: String? = null
    private var startYear: String? = null
    private var endYear: String? = null
    private var order: Int? = null
    private var desc: Boolean? = null

    private val mLoader: PaginatedDataLoader<MovieDataView> =
        object : PaginatedDataLoader<MovieDataView>() {

            override fun getLimit(): Int {
                return LIMIT
            }

            override fun loadDataFromDB(
                offset: Int,
                limit: Int
            ): List<MovieDataView> {
                if (videoTag == null) {
                    return emptyList()
                }

                return movieDao.queryMovieDataView(
                    shortcut?.uri,
                    videoTag?.vtid ?: -1,
                    genreName,
                    startYear,
                    endYear,
                    order ?: 4,
                    Config.getSqlConditionOfChildMode(),
                    desc ?: true,
                    ScraperSourceTools.getSource(),
                    offset,
                    limit
                )
            }

            override fun OnReloadResult(result: List<MovieDataView>) {
                mOnRefresh?.newSearch(result)
            }

            override fun OnLoadNextResult(result: List<MovieDataView>) {
                mOnRefresh?.appendMovieDataViews(result)
            }
        }


    var mOnRefresh: OnRefresh? = null

    fun reloadMovies() {
        mLoader.reload()
    }

    fun loadMoreMovies() {
        mLoader.loadNext()
    }

    fun forceReloadMovies() {
        mLoader.forceReload()
    }

    fun setOnRefresh(onRefresh: OnRefresh) {
        mOnRefresh = onRefresh
    }

    fun setVideoType(videoType: VideoType) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            if(videoTag==null) {
                videoTag = videoTagDao.queryVtidByNormalTag(videoType.name)
                reloadMovies()
            }
        }
    }

    fun setFilter(
        shortcut: Shortcut?,
        genre: String?,
        startYear: String?,
        endYear: String?
    ) {
        this.shortcut = shortcut
        this.genreName = genre
        this.startYear = startYear
        this.endYear = endYear
    }

    fun setOrder(order: Int, desc: Boolean) {
        this.order = order
        this.desc = desc
    }

    interface OnRefresh {
        fun beforeLoad()
        fun newSearch(newMovieDataView: List<MovieDataView?>?)
        fun appendMovieDataViews(movieDataViews: List<MovieDataView?>?)
    }


    companion object {
        const val TAG = "FilterViewModel"
        const val LIMIT = 15
    }
}