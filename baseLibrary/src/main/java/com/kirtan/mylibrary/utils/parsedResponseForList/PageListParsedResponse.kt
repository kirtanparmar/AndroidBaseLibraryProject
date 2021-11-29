package com.kirtan.mylibrary.utils.parsedResponseForList

import com.kirtan.mylibrary.utils.PagingListModel

data class PageListParsedResponse<ModelType : PagingListModel>(
    val isSuccess: Boolean,
    val errorMessage: String = "",
    val newTotalPagination: Int = 0,
    val newPageData: ArrayList<ModelType> = ArrayList()
)