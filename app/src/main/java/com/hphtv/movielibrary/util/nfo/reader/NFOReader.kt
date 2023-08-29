package com.hphtv.movielibrary.util.nfo.reader

import com.hphtv.movielibrary.util.nfo.NFOEntity
import java.io.InputStream

interface NFOReader {
    fun readFromXML(inputStream: InputStream): NFOEntity?
}