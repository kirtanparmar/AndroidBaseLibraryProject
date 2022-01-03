package com.kirtan.baseLibrary.base.viewModels

open class ApiListViewModel<ApiResponseType> : ApiCallingViewModel<ApiResponseType>() {
    var dataStatus: Status = Status.DataNotParsed

    enum class Status { DataParsed, DataNotParsed, DataParsing }
}