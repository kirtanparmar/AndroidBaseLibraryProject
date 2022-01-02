package com.kirtan.baseLibrary.utils.parsedResponseForList

import com.kirtan.baseLibrary.utils.PagingListModel

data class PageListParsedResponse<ModelType : PagingListModel>(
    val isSuccess: Boolean,
    val errorMessage: String = "",
    val newTotalPagination: Int = 0,
    val newPageData: ArrayList<ModelType> = ArrayList()
)