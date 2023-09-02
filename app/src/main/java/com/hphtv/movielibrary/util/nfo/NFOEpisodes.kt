package com.hphtv.movielibrary.util.nfo

import com.hphtv.movielibrary.roomdb.entity.Actor
import com.hphtv.movielibrary.roomdb.entity.Director
import com.hphtv.movielibrary.roomdb.entity.Writer

data class NFOEpisodes(
    val showtitle:String?=null,
    val title: String? = null,
    val originaltitle: String? = null,
    val season:String?=null,
    val episode:String?=null,
    val plot: String? = null,
    val runtime: String? = null,
    val poster: String? = null,
    val tmdbid: String? = null,
    val premiered:String?=null,
    val writers:MutableList<Writer> = mutableListOf(),
    val directors:MutableList<Director> = mutableListOf(),
    val actors: MutableList<Actor> = mutableListOf(),
):NFOEntity {
    override fun getNFOType(): NFOType {
        return NFOType.EPISODE
    }
}