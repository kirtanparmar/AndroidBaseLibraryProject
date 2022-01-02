package com.kirtan.baseLibrary.base.dataHolder

data class Operation(
    val operation: OperationStatus = OperationStatus.SUCCESS,
    val message: String = ""
)