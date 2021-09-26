package com.kirtan.mylibrary.base

interface ListPagingScreen<ModelType> {
    /**
     * @return first page position in api to be fetched.
     * */
    fun getFirstPagePosition(): Int

    /**
     * @return Model object with isLoaderModel set true.
     */
    fun getLoaderDataModel(): ModelType
}