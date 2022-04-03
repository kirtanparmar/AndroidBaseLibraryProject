package com.kirtan.baseLibrary.listHelper

import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

interface ListScreen<ModelType> {
    fun createAdapter(): ListAdapter<ModelType, *>

    fun getRecyclerView(): RecyclerView

    fun getCenterProgressBar(): ProgressBar?

    fun getErrorTextView(): TextView?

    fun getSwipeRefreshLayout(): SwipeRefreshLayout?

    fun onSwipeRefreshDoExtra()
}