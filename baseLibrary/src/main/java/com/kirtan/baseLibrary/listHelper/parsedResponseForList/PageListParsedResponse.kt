package com.kirtan.baseLibrary.listHelper.parsedResponseForList

import com.kirtan.baseLibrary.listHelper.dataHolder.PagingListModel

data class PageListParsedResponse<ModelType : PagingListModel>(
    val isSuccess: Boolean,
    val errorMessage: String = "",
    val newTotalPagination: Int = 0,
    val newPageData: ArrayList<ModelType> = ArrayList()
)