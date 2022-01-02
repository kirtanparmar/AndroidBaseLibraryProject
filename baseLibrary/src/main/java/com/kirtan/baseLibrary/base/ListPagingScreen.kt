package com.kirtan.baseLibrary.base

import com.kirtan.baseLibrary.base.activity.apiPagingList.PaginationOn

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