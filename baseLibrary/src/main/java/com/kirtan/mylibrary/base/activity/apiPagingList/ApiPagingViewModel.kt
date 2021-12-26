package com.kirtan.mylibrary.base.activity.apiPagingList

import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel

class ApiPagingViewModel<ApiResponseType> : ApiCallingViewModel<ApiResponseType>() {
    var paginationInfo: PaginationOnInfo =
        PaginationOnInfo(paginationOn = PaginationOn.ITEM_COUNT, total = 0)
    var dataLoading: Boolean = false
    var page: Int = -1
}