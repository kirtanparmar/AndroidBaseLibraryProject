package com.kirtan.mylibrary.base

import android.widget.ProgressBar
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textview.MaterialTextView

interface ListScreen<ModelType> {
    /**
     * {@link androidx.recyclerview.widget.ListAdapter} only use this adapter.
     * @return adapter object you want to use in the list.
     * @return type must be ListAdapter
     */
    fun createAdapter(): ListAdapter<ModelType, *>

    /**
     * @return the RecyclerView you are using in the layout.
     */
    fun getRecyclerView(): RecyclerView

    /**
     * @return should be the center aligned progressBar if you are using else can be null.
     */
    fun getCenterProgressBar(): ProgressBar?

    /**
     * @return should be the Textview you are using for displaying the error or any kind of the message. Or you can pass null if not using any.
     */
    fun getErrorTextView(): MaterialTextView?

    /**
     * @return should be the swipe refresh view if you are using any in your layout file else null.
     */
    fun getSwipeRefreshLayout(): SwipeRefreshLayout?

    fun onSwipeRefreshDoExtra()
}