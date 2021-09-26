package com.kirtan.mylibrary.base


abstract class MyArrayList<T> : ArrayList<T>() {
    override fun add(element: T): Boolean {
        return if (super.add(element)) {
            newItemAdded()
            true
        } else {
            false
        }
    }

    fun add(element: T, callBack: () -> Unit): Boolean {
        return if (super.add(element)) {
            newItemAdded(callBack)
            true
        } else {
            false
        }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        newItemAdded()
    }

    fun add(index: Int, element: T, callBack: () -> Unit) {
        super.add(index, element)
        newItemAdded(callBack)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return if (super.addAll(elements)) {
            if (elements.isEmpty()) {
                emptyListAdded()
            }
            newItemAdded()
            true
        } else {
            if (elements.isEmpty()) {
                emptyListAdded()
            }
            false
        }
    }

    fun addAll(elements: Collection<T>, callBack: () -> Unit): Boolean {
        return if (super.addAll(elements)) {
            if (elements.isEmpty()) {
                emptyListAdded(callBack)
            }
            newItemAdded(callBack)
            true
        } else {
            if (elements.isEmpty()) {
                emptyListAdded(callBack)
            }
            false
        }
    }

    override fun clear() {
        val lastListSize = size
        super.clear()
        listCleared(lastListSize)
    }

    fun clear(callBack: () -> Unit) {
        val lastListSize = size
        super.clear()
        listCleared(lastListSize, callBack)
    }

    override fun removeAt(index: Int): T {
        val removedModel = super.removeAt(index)
        if (removedModel != null) {
            itemRemovedAt(index)
        }
        return removedModel
    }

    fun removeAt(index: Int, callBack: () -> Unit): T {
        val removedModel = super.removeAt(index)
        if (removedModel != null) {
            itemRemovedAt(index, callBack)
        }
        return removedModel
    }

    override fun remove(element: T): Boolean {
        return if (super.remove(element)) {
            itemRemovedUnknownPosition()
            true
        } else {
            false
        }
    }

    fun remove(element: T, callBack: () -> Unit): Boolean {
        return if (super.remove(element)) {
            itemRemovedUnknownPosition(callBack)
            true
        } else {
            false
        }
    }

    abstract fun newItemAdded(callBack: () -> Unit = {})
    abstract fun listCleared(lastListSize: Int, callBack: () -> Unit = {})
    abstract fun emptyListAdded(callBack: () -> Unit = {})
    abstract fun itemRemovedAt(position: Int, callBack: () -> Unit = {})
    abstract fun itemRemovedUnknownPosition(callBack: () -> Unit = {})
}