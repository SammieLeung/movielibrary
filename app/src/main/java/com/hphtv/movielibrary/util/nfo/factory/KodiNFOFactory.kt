package com.hphtv.movielibrary.util.nfo.factory

import com.hphtv.movielibrary.util.nfo.reader.KodiNFOReader
import com.hphtv.movielibrary.util.nfo.reader.NFOReader
import com.hphtv.movielibrary.util.nfo.writer.NFOWriter

class KodiNFOFactory:NFOFactory{
    override fun createReader(): NFOReader {
        return KodiNFOReader()
    }

    override fun createWriter(): NFOWriter {
        TODO("Not yet implemented")
    }
}