package com.hphtv.movielibrary.util.nfo.reader

import com.hphtv.movielibrary.util.nfo.NFOInfo

interface NFOReader {
    fun readFromXML(xml: String): NFOInfo
}