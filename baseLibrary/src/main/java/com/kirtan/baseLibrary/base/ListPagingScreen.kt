package com.kirtan.baseLibrary.base

import com.kirtan.baseLibrary.base.activity.apiPagingList.PaginationOn

interface ListPagingScreen<ModelType> {
    fun getFirstPagePosition(): Int

    fun getLoaderDataModel(): ModelType

    fun getPaginationType(): PaginationOn
}