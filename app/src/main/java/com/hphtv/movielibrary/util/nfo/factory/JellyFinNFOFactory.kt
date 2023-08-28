package com.hphtv.movielibrary.util.nfo.factory

import com.hphtv.movielibrary.util.nfo.reader.JellyFinNFOReader
import com.hphtv.movielibrary.util.nfo.reader.NFOReader
import com.hphtv.movielibrary.util.nfo.writer.NFOWriter

class JellyFinNFOFactory:NFOFactory {
    override fun createReader(): NFOReader {
        return JellyFinNFOReader()
    }

    override fun createWriter(): NFOWriter {
        TODO("Not yet implemented")
    }
}