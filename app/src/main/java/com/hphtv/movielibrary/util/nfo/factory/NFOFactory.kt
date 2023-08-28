package com.hphtv.movielibrary.util.nfo.factory

import com.hphtv.movielibrary.util.nfo.reader.NFOReader
import com.hphtv.movielibrary.util.nfo.writer.NFOWriter

interface NFOFactory {
    fun createReader(): NFOReader
    fun createWriter(): NFOWriter
}