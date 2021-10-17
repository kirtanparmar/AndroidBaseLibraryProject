package com.kirtan.mylibrary.base.dataHolder

data class Operation(
    val operation: OperationStatus = OperationStatus.SUCCESS,
    val message: String = ""
)