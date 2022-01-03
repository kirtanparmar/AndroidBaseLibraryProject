package com.kirtan.baseLibrary.base.activity.apiPagingList

import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel

class ApiPagingViewModel<ApiResponseType> : ApiListViewModel<ApiResponseType>() {
    var paginationInfo: PaginationOnInfo =
        PaginationOnInfo(paginationOn = PaginationOn.ITEM_COUNT, total = 0)
    var page: Int = -1
}