package com.hphtv.movielibrary.data.pagination

abstract class BasePaginationCallback<T> : PaginationCallback<T> {

    override fun loading() {
    }

    override fun loadFinish() {
    }

    override fun onError(message: String) {
    }
}