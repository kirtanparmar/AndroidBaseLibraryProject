package com.kirtan.baseLibrary.base.viewModels

sealed class ApiStatus {
    object Init : ApiStatus()
    object Loading : ApiStatus()
    object Success : ApiStatus()
    class Error(val error: String, val errorCode: Int = -1) : ApiStatus()
}