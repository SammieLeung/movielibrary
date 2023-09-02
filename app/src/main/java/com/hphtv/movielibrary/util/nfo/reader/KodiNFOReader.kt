package com.hphtv.movielibrary.util.nfo.reader

import android.util.Xml
import com.hphtv.movielibrary.roomdb.entity.Actor
import com.hphtv.movielibrary.roomdb.entity.Director
import com.hphtv.movielibrary.roomdb.entity.Writer
import com.hphtv.movielibrary.util.nfo.NFOEntity
import com.hphtv.movielibrary.util.nfo.NFOEpisodes
import com.hphtv.movielibrary.util.nfo.NFOMovie
import com.hphtv.movielibrary.util.nfo.NFOTVShow
import com.orhanobut.logger.Logger
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class KodiNFOReader : NFOReader {
    override fun readFromXML(inputStream: InputStream): NFOEntity? {
        val xmlPullParser: XmlPullParser = Xml.newPullParser()
        xmlPullParser.setInput(inputStream, "UTF-8")
        var nfoInfo: NFOEntity? = null
        while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
            when (xmlPullParser.name) {
                "movie" -> {
                    nfoInfo = parseMovie(xmlPullParser)
                }

                "tvshow" -> {
                    nfoInfo = parseTvShow(xmlPullParser)
                }

                "episodedetails" -> {
                    nfoInfo = parseEpisodeDetail(xmlPullParser)
                }
            }
        }
        return nfoInfo
    }

    private fun parseEpisodeDetail(parser: XmlPullParser): NFOEpisodes? {
        var bean = NFOEpisodes()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {

                "title" -> {
                    bean = bean.copy(title = parseTitle(parser))
                    parser.require(XmlPullParser.END_TAG, null, "title")
                }

                "originaltitle" -> {
                    bean = bean.copy(originaltitle = parseOriginalTitle(parser))
                    parser.require(XmlPullParser.END_TAG, null, "originaltitle")
                }

                "showtitle" -> {
                    bean = bean.copy(showtitle = parseShowTitle(parser))
                    parser.require(XmlPullParser.END_TAG, null, "showtitle")
                }

                "season" -> {
                    parseSeason(parser)?.let {
                        bean = bean.copy(season = it)
                        parser.require(XmlPullParser.END_TAG, null, "season")
                    }
                }

                "episode" -> {
                    parseEpisode(parser)?.let {
                        bean = bean.copy(episode = it)
                        parser.require(XmlPullParser.END_TAG, null, "episode")
                    }
                }

                "uniqueid" -> {
                    parseTmdbIdFromUniqueid(parser)?.let {
                        bean = bean.copy(tmdbid = it)
                        parser.require(XmlPullParser.END_TAG, null, "uniqueid")
                    }
                }

                "plot" -> {
                    bean = bean.copy(plot = parsePlot(parser))
                    parser.require(XmlPullParser.END_TAG, null, "plot")
                }

                "runtime" -> {
                    bean = bean.copy(runtime = parseRuntime(parser))
                    parser.require(XmlPullParser.END_TAG, null, "runtime")
                }

                "thumb" -> {
                    parser.require(XmlPullParser.START_TAG, null, "thumb")
                    parsePoster(parser).let { bean = bean.copy(poster = it) }
                    parser.require(XmlPullParser.END_TAG, null, "thumb")
                }


                "premiered" -> {
                    bean = bean.copy(premiered = parsePremiered(parser))
                    parser.require(XmlPullParser.END_TAG, null, "premiered")
                }


                "credits" -> {
                    bean.writers.add(parseCredits(parser))
                    parser.require(XmlPullParser.END_TAG, null, "credits")
                }

                "director" -> {
                    bean.directors.add(parseDirector(parser))
                    parser.require(XmlPullParser.END_TAG, null, "director")
                }

                "actor" -> {
                    bean.actors.add(parseActor(parser))
                    parser.require(XmlPullParser.END_TAG, null, "actor")
                }

                else -> {
                    skip(parser)
                }

            }
        }
        Logger.d(bean)
        return bean
    }

    private fun parseTvShow(parser: XmlPullParser): NFOTVShow {
        var bean = NFOTVShow()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag.
            when (parser.name) {

                "showtitle" -> {
                    bean = bean.copy(showtitle = parseShowTitle(parser))
                }

                "title" -> {
                    bean = bean.copy(title = parseTitle(parser))
                    parser.require(XmlPullParser.END_TAG, null, "title")
                }

                "originaltitle" -> {
                    bean = bean.copy(originaltitle = parseOriginalTitle(parser))
                    parser.require(XmlPullParser.END_TAG, null, "originaltitle")
                }

                "year" -> {
                    bean = bean.copy(year = parseYear(parser))
                    parser.require(XmlPullParser.END_TAG, null, "year")
                }

                "ratings" -> {
                    bean = bean.copy(ratings = parseRatings(parser))
                    parser.require(XmlPullParser.END_TAG, null, "ratings")
                }

                "rating" -> {
                    bean = bean.copy(ratings = parseTVShowRating(parser))
                    parser.require(XmlPullParser.END_TAG, null, "rating")
                }

                "plot" -> {
                    bean = bean.copy(plot = parsePlot(parser))
                    parser.require(XmlPullParser.END_TAG, null, "plot")
                }

                "runtime" -> {
                    bean = bean.copy(runtime = parseRuntime(parser))
                    parser.require(XmlPullParser.END_TAG, null, "runtime")
                }

                "thumb" -> {
                    parser.require(XmlPullParser.START_TAG, null, "thumb")
                    if (parser.getAttributeValue(null, "aspect") == "poster") {
                        if (parser.getAttributeValue(null, "type") == "season") {
                            bean.seasonPosters.putAll(parseSeasonPoster(parser))
                        } else {
                            bean = bean.copy(poster = parsePoster(parser))
                        }
                        parser.require(XmlPullParser.END_TAG, null, "thumb")
                    } else {
                        skip(parser)
                    }
                }

                "namedseason" -> {
                    bean.seasonNames.putAll(parseNamedSeason(parser))
                }

                "fanart" -> {
                    bean = bean.copy(fanart = parseFanart(parser))
                    parser.require(XmlPullParser.END_TAG, null, "fanart")
                }

                "tmdbid" -> {
                    bean = bean.copy(tmdbid = parseTmdbid(parser))
                    parser.require(XmlPullParser.END_TAG, null, "tmdbid")
                }

                "country" -> {
                    parseCountry(parser)?.let { bean.countries.add(it) }
                    parser.require(XmlPullParser.END_TAG, null, "country")
                }

                "premiered" -> {
                    bean = bean.copy(premiered = parsePremiered(parser))
                    parser.require(XmlPullParser.END_TAG, null, "premiered")
                }

                "genre" -> {
                    bean.genres.add(parseGenre(parser))
                    parser.require(XmlPullParser.END_TAG, null, "genre")
                }

                "actor" -> {
                    bean.actors.add(parseActor(parser))
                    parser.require(XmlPullParser.END_TAG, null, "actor")
                }

                "directors" -> {
                    bean.directors.add(parseDirector(parser))
                    parser.require(XmlPullParser.END_TAG, null, "directors")
                }

                else -> {
                    skip(parser)
                }
            }
        }
        Logger.d(bean)
        return bean
    }


    private fun parseMovie(parser: XmlPullParser): NFOMovie {
        var bean = NFOMovie()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag.
            when (parser.name) {
                "title" -> {
                    bean = bean.copy(title = parseTitle(parser))
                    parser.require(XmlPullParser.END_TAG, null, "title")
                }

                "originaltitle" -> {
                    bean = bean.copy(originaltitle = parseOriginalTitle(parser))
                    parser.require(XmlPullParser.END_TAG, null, "originaltitle")
                }

                "year" -> {
                    bean = bean.copy(year = parseYear(parser))
                    parser.require(XmlPullParser.END_TAG, null, "year")
                }

                "ratings" -> {
                    bean = bean.copy(ratings = parseRatings(parser))
                    parser.require(XmlPullParser.END_TAG, null, "ratings")
                }

                "plot" -> {
                    bean = bean.copy(plot = parsePlot(parser))
                    parser.require(XmlPullParser.END_TAG, null, "plot")
                }

                "runtime" -> {
                    bean = bean.copy(runtime = parseRuntime(parser))
                    parser.require(XmlPullParser.END_TAG, null, "runtime")
                }

                "thumb" -> {
                    parser.require(XmlPullParser.START_TAG, null, "thumb")
                    parsePoster(parser).let { bean = bean.copy(poster = it) }
                    parser.require(XmlPullParser.END_TAG, null, "thumb")
                }

                "fanart" -> {
                    bean = bean.copy(fanart = parseFanart(parser))
                    parser.require(XmlPullParser.END_TAG, null, "fanart")
                }

                "tmdbid" -> {
                    bean = bean.copy(tmdbid = parseTmdbid(parser))
                    parser.require(XmlPullParser.END_TAG, null, "tmdbid")
                }

                "country" -> {
                    parseCountry(parser)?.let { bean.countries.add(it) }
                    parser.require(XmlPullParser.END_TAG, null, "country")
                }

                "premiered" -> {
                    bean = bean.copy(premiered = parsePremiered(parser))
                    parser.require(XmlPullParser.END_TAG, null, "premiered")
                }

                "genre" -> {
                    bean.genres.add(parseGenre(parser))
                    parser.require(XmlPullParser.END_TAG, null, "genre")
                }

                "credits" -> {
                    bean.writers.add(parseCredits(parser))
                    parser.require(XmlPullParser.END_TAG, null, "credits")
                }

                "director" -> {
                    bean.directors.add(parseDirector(parser))
                    parser.require(XmlPullParser.END_TAG, null, "director")
                }

                "actor" -> {
                    bean.actors.add(parseActor(parser))
                    parser.require(XmlPullParser.END_TAG, null, "actor")
                }

                "languages" -> {
                    bean = bean.copy(languages = parseLanguages(parser))
                    parser.require(XmlPullParser.END_TAG, null, "languages")
                }

                else -> {
                    skip(parser)
                }
            }
        }
        Logger.d(bean)
        return bean
    }

    private fun parseShowTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "showtitle")
        return parser.nextText()
    }

    private fun parseTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "title")
        return parser.nextText()
    }

    private fun parseOriginalTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "originaltitle")
        return parser.nextText()
    }

    private fun parseSeason(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "season")
        return parser.nextText()
    }

    private fun parseEpisode(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "episode")
        return parser.nextText()
    }

    private fun parseTmdbIdFromUniqueid(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "uniqueid")
        if (parser.getAttributeValue(null, "type") == "tmdb") {
            return parser.nextText()
        }
        skip(parser)
        return null;
    }

    private fun parseYear(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "year")
        return parser.nextText()
    }

    private fun parseTVShowRating(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "rating")
        return parser.nextText()
    }

    private fun parseRatings(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "ratings")
        var depth = 1
        var rating = ""
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> {
                    depth--
                }

                XmlPullParser.START_TAG -> {
                    depth++
                    when (parser.name) {
                        "rating" -> {
                            if (parser.getAttributeValue(null, "name") == "themoviedb") {
                                while (parser.next() != XmlPullParser.END_TAG) {
                                    if (parser.eventType != XmlPullParser.START_TAG) {
                                        continue
                                    }
                                    when (parser.name) {
                                        "value" -> {
                                            rating = parser.nextText()
                                        }

                                        else -> {
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                        }

                    }
                }

                else -> {
                }
            }
        }
        return rating
    }

    private fun parseRuntime(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "runtime")
        return parser.nextText()
    }

    private fun parseSeasonPoster(parser: XmlPullParser): Map<Int, String> {
        val season = parser.getAttributeValue(null, "season").toInt()
        val poster = parser.nextText()
        return mapOf(season to poster)
    }


    private fun parseNamedSeason(parser: XmlPullParser): Map<Int, String> {
        parser.require(XmlPullParser.START_TAG, null, "namedseason")
        val season = parser.getAttributeValue(null, "number").toInt()
        val name = parser.nextText()
        parser.require(XmlPullParser.END_TAG, null, "namedseason")
        return mapOf(season to name)
    }

    private fun parsePoster(parser: XmlPullParser): String {
        return parser.nextText()
    }

    private fun parseFanart(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "fanart")
        var fanart = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "thumb" -> {
                    fanart = parser.nextText()
                }

                else -> {}
            }

        }
        return fanart
    }

    private fun parseTmdbid(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "tmdbid")
        return parser.nextText()
    }

    private fun parseCountry(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "country")
        return parser.nextText()
    }

    private fun parsePremiered(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "premiered")
        return parser.nextText()
    }

    private fun parseGenre(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "genre")
        return parser.nextText()
    }

    private fun parseCredits(parser: XmlPullParser): Writer {
        parser.require(XmlPullParser.START_TAG, null, "credits")
        val writerId = parser.getAttributeValue(null, "tmdbid")
        val name = parser.nextText()
        val writer = Writer()
        writer.name = name
        writer.nameEn = name
        writer.writerId = writerId.toLong()
        return writer

    }

    private fun parseDirector(parser: XmlPullParser): Director {
        parser.require(XmlPullParser.START_TAG, null, "director")
        val directorId = parser.getAttributeValue(null, "tmdbid")
        val name = parser.nextText()
        val director = Director()
        director.name = name
        director.nameEn = name
        director.director_id = directorId.toLong()
        return director
    }

    private fun parseActor(parser: XmlPullParser): Actor {
        parser.require(XmlPullParser.START_TAG, null, "actor")
        val actor = Actor()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "name" -> {
                    val name = parser.nextText()
                    actor.name = name
                    actor.nameEn = name
                }

                "thumb" -> {
                    actor.img = parser.nextText()
                }

                "tmdbid" -> {
                    actor.actorId = parser.nextText().toLong()
                }

                else -> {
                    skip(parser)
                }
            }
        }
        return actor
    }

    private fun parseLanguages(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "languages")
        return parser.nextText()
    }

    private fun parsePlot(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "plot")
        return parser.nextText()
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}