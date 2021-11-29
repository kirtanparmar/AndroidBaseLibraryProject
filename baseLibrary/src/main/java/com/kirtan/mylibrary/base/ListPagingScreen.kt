package com.kirtan.mylibrary.base

import com.kirtan.mylibrary.base.activity.apiPagingList.PaginationOn

interface ListPagingScreen<ModelType> {
    /**
     * @return first page position in api to be fetched.
     * */
    fun getFirstPagePosition(): Int

    /**
     * @return Model object with isLoaderModel set true.
     */
    fun getLoaderDataModel(): ModelType

    fun getPaginationType(): PaginationOn
}