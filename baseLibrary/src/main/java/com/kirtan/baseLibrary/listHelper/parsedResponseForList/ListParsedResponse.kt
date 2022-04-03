package com.kirtan.baseLibrary.listHelper.parsedResponseForList

data class ListParsedResponse<ModelType>(
    val isSuccess: Boolean,
    val errorMessage: String = "",
    val apiListData: ArrayList<ModelType> = ArrayList()
)