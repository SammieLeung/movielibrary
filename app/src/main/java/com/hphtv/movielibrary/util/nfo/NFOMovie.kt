package com.hphtv.movielibrary.util.nfo

import com.hphtv.movielibrary.data.Constants
import com.hphtv.movielibrary.roomdb.entity.Actor
import com.hphtv.movielibrary.roomdb.entity.Director
import com.hphtv.movielibrary.roomdb.entity.Genre
import com.hphtv.movielibrary.roomdb.entity.GenreTag
import com.hphtv.movielibrary.roomdb.entity.Movie
import com.hphtv.movielibrary.roomdb.entity.Writer

data class NFOMovie(
    val title: String? = null,
    val originaltitle: String? = null,
    val year: String? = null,
    val ratings: String? = null,
    val plot: String? = null,
    val runtime: String? = null,
    val poster: String? = null,
    val fanart: String? = null,
    val tmdbid: String? = null,
    val countries: MutableList<String> = mutableListOf(),
    val premiered: String? = null,
    val genres: MutableList<String> = mutableListOf(),
    val directors: MutableList<Director> = mutableListOf(),
    val writers: MutableList<Writer> = mutableListOf(),
    val actors: MutableList<Actor> = mutableListOf(),
    val languages: String? = null,
) : NFOEntity {
    override fun getNFOType(): NFOType {
        return NFOType.MOVIE
    }
}

fun NFOMovie.toMovie(): Movie {
    val that = this
    val movie = Movie()
    movie.apply {
        this.movieId = that.tmdbid
        this.title = that.title
        this.otherTitle = that.originaltitle
        this.plot = that.plot
        this.ratings = that.ratings
        this.source = Constants.Scraper.TMDB
        this.type = Constants.VideoType.movie
        this.poster = that.poster
        this.releaseDate = that.premiered
        this.year = that.year
        this.duration = that.runtime
    }
    return movie
}

