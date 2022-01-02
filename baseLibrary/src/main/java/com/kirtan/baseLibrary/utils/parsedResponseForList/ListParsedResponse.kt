package com.kirtan.baseLibrary.utils.parsedResponseForList

data class ListParsedResponse<ModelType>(
    val isSuccess: Boolean,
    val errorMessage: String = "",
    val apiListData: ArrayList<ModelType> = ArrayList()
)