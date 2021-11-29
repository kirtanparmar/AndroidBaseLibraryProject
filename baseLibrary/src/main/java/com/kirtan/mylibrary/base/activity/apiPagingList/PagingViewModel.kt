package com.kirtan.mylibrary.base.activity.apiPagingList

import androidx.lifecycle.ViewModel

class PagingViewModel : ViewModel() {
    var paginationInfo: PaginationOnInfo =
        PaginationOnInfo(paginationOn = PaginationOn.ITEM_COUNT, total = 0)
    var dataLoading: Boolean = false
    var page: Int = -1
}