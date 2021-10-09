package com.kirtan.mylibrary.base.dataHolder

import timber.log.Timber

abstract class BaseArrayList<T : BaseObject> : ArrayList<T>() {
    override fun clear() = clear {}

    fun clear(callBack: (operation: Operation) -> Unit) {
        val lastListSize = size
        super.clear()
        if (size > 0) "Failed to clear the list.".operateOnFail(callBack)
        else listCleared(lastListSize, callBack)
    }

    override fun add(element: T): Boolean {
        return try {
            if (super.add(element)) {
                newItemAdded(size - 1)
                true
            } else {
                "Failed to add element $element into the list".operateOnFail {}
                false
            }
        } catch (e: Exception) {
            e.operateOnError {}
            false
        }
    }

    override fun add(index: Int, element: T) = add(index, element) {}

    override fun addAll(elements: Collection<T>): Boolean = addAll(size, elements)

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return if (elements.isEmpty()) {
            emptyListAdded()
            true
        } else try {
            if (super.addAll(index, elements)) {
                newItemRangeAdded(
                    index,
                    index + elements.size - 1
                )
                true
            } else {
                "Failed to add elements into the list.".operateOnFail {}
                false
            }
        } catch (e: Exception) {
            e.operateOnError {}
            false
        }
    }

    fun add(element: T, callBack: (operation: Operation) -> Unit) {
        try {
            if (super.add(element)) newItemAdded(size - 1, callBack)
            else "Failed to add element $element into the list".operateOnFail(callBack)
        } catch (e: Exception) {
            e.operateOnError(callBack)
        }
    }

    fun add(index: Int, element: T, callBack: (operation: Operation) -> Unit) {
        try {
            val oldSize = size
            super.add(index, element)
            if (oldSize < size) newItemAdded(index, callBack)
            else "Failed to add element $element at $index number index of the list."
                .operateOnFail(callBack)
        } catch (e: Exception) {
            e.operateOnError(callBack)
        }
    }

    fun addAll(elements: Collection<T>, callBack: (operation: Operation) -> Unit) =
        addAll(size, elements, callBack)

    fun addAll(index: Int, elements: Collection<T>, callBack: (operation: Operation) -> Unit) {
        if (elements.isEmpty()) emptyListAdded(callBack)
        else try {
            if (super.addAll(index, elements)) newItemRangeAdded(
                index,
                index + elements.size - 1,
                callBack
            )
            else "Failed to add elements into the list.".operateOnFail(callBack)
        } catch (e: Exception) {
            e.operateOnError(callBack)
        }
    }

    override fun remove(element: T): Boolean = remove(element) {}

    override fun removeAt(index: Int): T = removeAt(index) {}

    fun remove(element: T, callBack: (operation: Operation) -> Unit): Boolean {
        return if (size == 0) {
            listCleared(size, callBack)
            true
        } else try {
            return if (super.remove(element)) {
                itemRemovedUnknownPosition(callBack)
                true
            } else {
                "Failed to remove the element $element from list".operateOnFail(callBack)
                false
            }
        } catch (e: Exception) {
            e.operateOnError(callBack)
            false
        }
    }

    fun removeAt(index: Int, callBack: (operation: Operation) -> Unit): T {
        return try {
            val element = super.removeAt(index)
            itemRemovedAt(index, callBack)
            element
        } catch (e: Exception) {
            e.operateOnError(callBack)
            emptyObjectForNullAssertion().apply { nullObject = true }
        }
    }

    abstract fun newItemAdded(position: Int, callBack: (operation: Operation) -> Unit = {})
    abstract fun newItemRangeAdded(
        start: Int,
        end: Int,
        callBack: (operation: Operation) -> Unit = {}
    )

    abstract fun listCleared(lastListSize: Int, callBack: (operation: Operation) -> Unit = {})
    abstract fun emptyListAdded(callBack: (operation: Operation) -> Unit = {})
    abstract fun itemRemovedAt(position: Int, callBack: (operation: Operation) -> Unit = {})
    abstract fun itemRemovedUnknownPosition(callBack: (operation: Operation) -> Unit = {})
    abstract fun emptyObjectForNullAssertion(): T

    private fun Exception.operateOnError(callBack: (operation: Operation) -> Unit) {
        printStackTrace()
        Timber.e(this)
        callBack.invoke(Operation(OperationStatus.ERROR, "Error: $message"))
    }

    private fun String.operateOnFail(callBack: (operation: Operation) -> Unit) {
        callBack.invoke(Operation(OperationStatus.FAIL, this))
    }
}