package com.hphtv.movielibrary.util.nfo

import com.hphtv.movielibrary.roomdb.entity.Actor
import com.hphtv.movielibrary.roomdb.entity.Director
import com.hphtv.movielibrary.roomdb.entity.Writer

data class NFOEntity(
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
    val directors:MutableList<Director> = mutableListOf(),
    val writers: MutableList<Writer> = mutableListOf(),
    val actors: MutableList<Actor> = mutableListOf(),
    val languages: String? = null,
)