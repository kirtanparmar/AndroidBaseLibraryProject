package com.kirtan.mylibrary.base.dataHolder

interface OnArrayListOperations<T> {
    fun newItemAdded(position: Int, callBack: (operation: Operation) -> Unit = {})
    fun listCleared(lastListSize: Int, callBack: (operation: Operation) -> Unit = {})
    fun emptyListAdded(callBack: (operation: Operation) -> Unit = {})
    fun itemRemovedAt(position: Int, callBack: (operation: Operation) -> Unit = {})
    fun itemRemovedUnknownPosition(element: T, callBack: (operation: Operation) -> Unit = {})
    fun emptyObjectForNullAssertion(): T
    fun newItemRangeAdded(
        start: Int,
        end: Int,
        callBack: (operation: Operation) -> Unit = {}
    )
}