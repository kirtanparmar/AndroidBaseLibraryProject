package com.kirtan.baseLibrary.listHelper.dataHolder

sealed class Operation {
    object Success : Operation()
    class Error(val message: String = "") : Operation()
    class Fail(val message: String = "") : Operation()
}