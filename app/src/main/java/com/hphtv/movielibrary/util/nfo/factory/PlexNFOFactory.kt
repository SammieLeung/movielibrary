package com.hphtv.movielibrary.util.nfo.factory

import com.hphtv.movielibrary.util.nfo.reader.NFOReader
import com.hphtv.movielibrary.util.nfo.reader.PlexNFOReader
import com.hphtv.movielibrary.util.nfo.writer.NFOWriter

class PlexNFOFactory:NFOFactory {
    override fun createReader(): NFOReader {
        return PlexNFOReader()
    }

    override fun createWriter(): NFOWriter {
        TODO("Not yet implemented")
    }
}