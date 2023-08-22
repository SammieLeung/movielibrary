package com.hphtv.movielibrary.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.firefly.videonameparser.VideoNameParserV2
import com.hphtv.movielibrary.data.Constants
import com.hphtv.movielibrary.data.Constants.DeviceType
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase
import com.hphtv.movielibrary.roomdb.dao.DeviceDao
import com.hphtv.movielibrary.roomdb.dao.MovieDao
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao
import com.hphtv.movielibrary.roomdb.entity.Shortcut
import com.hphtv.movielibrary.roomdb.entity.VideoFile
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone
import com.hphtv.movielibrary.scraper.service.TmdbApiService
import com.hphtv.movielibrary.util.MovieHelper
import com.hphtv.movielibrary.util.rxjava.SimpleObserver
import com.orhanobut.logger.Logger
import com.station.kit.util.FileUtils
import java.io.File

class MovieLibraryProviderV2 : ContentProvider() {
    var matcher: UriMatcher? = null
    override fun onCreate(): Boolean {
        matcher = UriMatcher(UriMatcher.NO_MATCH);
        matcher?.addURI(AUTHORITY, "addPoster", ADD_POSTER)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Logger.d("MovieLibraryProviderV2", "insert")
        val appContext = context?.applicationContext ?: return null
        val dataBase = MovieLibraryRoomDatabase.getDatabase(appContext)
        when (matcher?.match(uri)) {
            ADD_POSTER -> {
                preMatch(
                    dataBase.deviceDao,
                    dataBase.shortcutDao,
                    dataBase.videoFileDao,
                    dataBase.movieDao,
                    values
                )
            }
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return -1
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return -1
    }

    private fun preMatch(
        deviceDao: DeviceDao,
        shortcutDao: ShortcutDao,
        videoFileDao: VideoFileDao,
        movieDao: MovieDao,
        values: ContentValues?
    ) {
        var downloadPath = values?.getAsString("download_path")
        val movieId = values?.getAsString("movie_id")
        val movieType = values?.getAsString("movie_type")
        val fileList = values?.getAsString("file_list") ?: return
        val filePaths = fileList.split(";;").filter { FileUtils.isMediaFile(it) }
        if (filePaths.isEmpty())
            return
        if (movieId == null)
            return

        var devicePath: String? = null
        var shortcutUri: String? = null
        if (downloadPath != null) {
            val shortcut = getShortcutUriInDB(downloadPath, filePaths, shortcutDao)
            if (shortcut != null) {
                shortcutUri=shortcut.uri
                devicePath = shortcut.devicePath
                shortcutDao.updateShortcut(shortcut)
            } else {
                shortcutUri = getCommonShortcutUri(downloadPath, filePaths)
                if (shortcutUri != null) {
                    devicePath = addShortcut(shortcutDao, deviceDao, shortcutUri)
                } else {
                    Logger.e("No mount device found")
                }
            }
        }

        devicePath?.let {
            if (shortcutUri==null){
                Logger.e("No shortcut uri found")
                return
            }
            val videoFileList = addVideoFile(videoFileDao, filePaths, shortcutUri, it)
            val type = movieType?.lowercase() ?: return

            //3.查询/插入电影
            val wrapper =
                movieDao.queryWrapperByMovieIdAndType(movieId, Constants.Scraper.TMDB, type)
            if (wrapper == null) {
                TmdbApiService.getDetail(movieId, Constants.Scraper.TMDB, type)
                    .subscribe(object : SimpleObserver<MovieDetailRespone?>() {
                        override fun onAction(response: MovieDetailRespone?) {
                            val wrapper = response?.toEntity() ?: return
                            MovieHelper.manualSaveMovie(context, wrapper, videoFileList)
                        }
                    })
            } else {
                MovieHelper.manualSaveMovie(context, wrapper, videoFileList)
            }


            val wrapperEn =
                movieDao.queryWrapperByMovieIdAndType(movieId, Constants.Scraper.TMDB_EN, type)
            if (wrapperEn == null) {
                TmdbApiService.getDetail(
                    movieId, Constants.Scraper.TMDB_EN, type
                )
                    .subscribe(object : SimpleObserver<MovieDetailRespone?>() {
                        override fun onAction(response: MovieDetailRespone?) {
                            val wrapper = response?.toEntity()
                            MovieHelper.manualSaveMovie(context, wrapper, videoFileList)
                        }
                    })
            } else {
                MovieHelper.manualSaveMovie(context, wrapperEn, videoFileList)
            }
        }
    }

    private fun getShortcutUriInDB(
        downloadPath: String,
        fileList: List<String>,
        shortcutDao: ShortcutDao
    ): Shortcut? {
        val shortcutList = shortcutDao.queryAllLocalShortcuts()
        for (shortcut in shortcutList) {
            var count = 0
            for (filePath in fileList) {
                if (File(downloadPath, filePath).path.startsWith(shortcut.uri)) {
                    count++
                }
            }
            if (count == fileList.size)
                return shortcut
        }
        return null
    }

    private fun getCommonShortcutUri(downloadPath: String, filePaths: List<String>): String? {
        var shortcutUri: String? = null
        for (filePath in filePaths) {
            val realFilePath = File(downloadPath, filePath).path
            if (shortcutUri == null) {
                shortcutUri = realFilePath.substring(0, realFilePath.lastIndexOf("/"))
                continue
            }
            if (shortcutUri.split("/").size > realFilePath.split("/").size)
                shortcutUri = realFilePath.substring(0, realFilePath.lastIndexOf("/"))
        }
        return shortcutUri
    }


    private fun addShortcut(
        shortcutDao: ShortcutDao,
        deviceDao: DeviceDao,
        shortcutUri: String?
    ): String? {
        val shortcut =
            Shortcut(shortcutUri, DeviceType.DEVICE_TYPE_LOCAL, null, null, shortcutUri)
        val mountDevice = deviceDao.qureyAll()?.find {
            if (shortcutUri?.startsWith(it.path) == true && isLocalDeviceType(it.type))
                return@find true
            return@find false
        } ?: return null
        shortcut.devicePath = mountDevice.path
        shortcut.deviceType = mountDevice.type
        shortcut.isScanned=1
        shortcutDao.insertShortcut(shortcut)
        return shortcut.devicePath
    }

    private fun addVideoFile(
        videoFileDao: VideoFileDao,
        filePaths: List<String>,
        shortcutUri: String,
        devicePath: String
    ): List<VideoFile> {
        val videoFileList = mutableListOf<VideoFile>()
        for (relativeFilePath in filePaths) {
            var videoFile = VideoFile()
            videoFile.filename = relativeFilePath.substring(relativeFilePath.lastIndexOf("/") + 1)
            videoFile.path = File(shortcutUri,relativeFilePath).path
            videoFile.dirPath = shortcutUri
            videoFile.devicePath = devicePath
            val parser = VideoNameParserV2()
            val nameInfo = parser.parseVideoName(videoFile.path)
            val keyword = nameInfo.name
            videoFile.keyword = keyword
            videoFile.season = nameInfo.season
            videoFile.episode = nameInfo.toEpisode()
            videoFile.aired = nameInfo.aired
            if (nameInfo.resolution != null) {
                videoFile.resolution = nameInfo.resolution
            }
            if (nameInfo.videoSource != null) {
                videoFile.videoSource = nameInfo.videoSource
            }
            //2.3、添加文件入数据库
            val vid: Long = videoFileDao.insertOrIgnore(videoFile)
            if (vid < 0) {
                videoFile = videoFileDao.queryByPath(relativeFilePath)
            } else {
                videoFile.vid = vid
            }
            videoFileList.add(videoFile)
        }
        return videoFileList
    }

    private fun isLocalDeviceType(deviceType: Int): Boolean {
        return deviceType != DeviceType.DEVICE_TYPE_DLNA && deviceType != DeviceType.DEVICE_TYPE_SMB
    }

    companion object {
        const val AUTHORITY = "com.hphtv.movielibrary.provider.v2"
        const val ADD_POSTER = 1

    }


}