package com.kirtan.mylibrary.base.viewModels

import androidx.lifecycle.ViewModel

class ApiListViewModel : ViewModel() {
    var status: Status = Status.DataNotParsed

    enum class Status { DataParsed, DataNotParsed, DataParsing }
}