package com.hphtv.movielibrary.util.nfo.factory

import com.hphtv.movielibrary.util.nfo.reader.EmbyNFOReader
import com.hphtv.movielibrary.util.nfo.reader.NFOReader
import com.hphtv.movielibrary.util.nfo.writer.NFOWriter

class EmbyNFOFactory : NFOFactory {
    override fun createReader(): NFOReader {
        return EmbyNFOReader()
    }

    override fun createWriter(): NFOWriter {
        TODO("Not yet implemented")
    }
}