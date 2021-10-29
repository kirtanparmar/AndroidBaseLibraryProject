package com.kirtan.mylibrary.base.activity.apiPagingList

import androidx.lifecycle.ViewModel

class PagingViewModel : ViewModel() {
    var totalItems: Int = 0
    var dataLoading: Boolean = false
    var page: Int = -1
}