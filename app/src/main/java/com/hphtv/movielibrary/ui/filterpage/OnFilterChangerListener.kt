package com.hphtv.movielibrary.ui.filterpage

import com.hphtv.movielibrary.roomdb.entity.Shortcut
import com.hphtv.movielibrary.roomdb.entity.VideoTag

interface OnFilterChangerListener {
    fun onFilterChange(shortcut: Shortcut?, genre: String?, startYear: String?,endYear:String?)
    fun onOrderChange(order: Int, isDesc: Boolean)
}