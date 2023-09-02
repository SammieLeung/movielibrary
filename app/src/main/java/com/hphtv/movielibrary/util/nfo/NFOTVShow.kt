package com.hphtv.movielibrary.util.nfo

import com.hphtv.movielibrary.data.Constants
import com.hphtv.movielibrary.roomdb.entity.Actor
import com.hphtv.movielibrary.roomdb.entity.Director
import com.hphtv.movielibrary.roomdb.entity.Genre
import com.hphtv.movielibrary.roomdb.entity.Movie
import com.hphtv.movielibrary.roomdb.entity.Season
import com.hphtv.movielibrary.roomdb.entity.StagePhoto
import com.hphtv.movielibrary.roomdb.entity.Writer

data class NFOTVShow(
    val showtitle:String?=null,
    val title: String? = null,
    val originaltitle: String? = null,
    val year: String? = null,
    val ratings: String? = null,
    val plot: String? = null,
    val runtime: String? = null,
    val poster: String? = null,
    val seasonNames: MutableMap<Int,String> = mutableMapOf(),
    val seasonPosters: MutableMap<Int,String> = mutableMapOf(),
    val fanart: String? = null,
    val tmdbid: String? = null,
    val countries: MutableList<String> = mutableListOf(),
    val premiered: String? = null,
    val genres: MutableList<String> = mutableListOf(),
    val actors: MutableList<Actor> = mutableListOf(),
    val directors: MutableList<Director> = mutableListOf()
) : NFOEntity {
    override fun getNFOType(): NFOType {
        return NFOType.TVSHOW
    }
}

fun NFOTVShow.toMovie(): Movie {
    val that=this
    val movie = Movie()
    movie.apply {
        this.movieId = that.tmdbid
        this.title = that.title
        this.otherTitle = that.originaltitle
        this.plot = that.plot
        this.ratings = that.ratings
        this.source = Constants.Scraper.TMDB
        this.type = Constants.VideoType.tv
        this.poster = that.poster
        this.releaseDate = that.premiered
        this.year = that.year
        this.duration = that.runtime
    }
    return movie
}

fun NFOTVShow.toGenreList(): List<Genre> {
    return this.genres.map {
        val genre = Genre()
        genre.name = it
        genre.source = Constants.Scraper.TMDB
        genre
    }
}

fun NFOTVShow.toStagePhotoList(): List<StagePhoto> {
    return this.fanart?.let {
        val stagePhoto = StagePhoto()
        stagePhoto.imgUrl = it
        listOf(stagePhoto)
    } ?: emptyList()
}

fun NFOTVShow.toSeasonList():List<Season>{
    return this.seasonNames.map {
        val season=Season()
        season.seasonNumber=it.key
        season.name=it.value
        season.airDate=premiered
        season.plot=this.plot
        season.poster=this.seasonPosters[it.key]
        season.source=Constants.Scraper.TMDB
        season.airDate=this.premiered
        season
    }
}
