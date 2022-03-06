package com.kirtan.baseLibrary.base.dataHolder

sealed class Operation {
    object Success : Operation()
    class Error(val message: String = "") : Operation()
    class Fail(val message: String = "") : Operation()
}