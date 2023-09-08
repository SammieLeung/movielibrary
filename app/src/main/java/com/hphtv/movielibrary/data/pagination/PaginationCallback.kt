package com.hphtv.movielibrary.data.pagination

import com.hphtv.movielibrary.roomdb.entity.dataview.UnknownRootDataView

interface PaginationCallback<T> {
    fun loading(){}
    fun onResult(roots: List<T>)
    fun loadFinish()
    fun onError(message:String)
}