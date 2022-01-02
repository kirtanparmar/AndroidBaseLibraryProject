package com.kirtan.baseLibrary.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kirtan.baseLibrary.databinding.DummyListLoaderBinding
import com.kirtan.baseLibrary.utils.PagingListModel

abstract class BaseAdapter<VIEW : ViewDataBinding, T : PagingListModel, VH : RecyclerView.ViewHolder>
    (diffCallback: DiffUtil.ItemCallback<T>) :
    ListAdapter<T, RecyclerView.ViewHolder>(diffCallback) {
    @get:LayoutRes
    abstract val resource: Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1)
            LoaderViewHolder(
                DummyListLoaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        else
            createViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    resource, parent, false
                )
            )
    }

    abstract fun createViewHolder(view: VIEW): VH

    abstract inner class BaseViewHolder(protected val view: VIEW) :
        RecyclerView.ViewHolder(view.root) {
        var mPosition = -1
        fun onBind(position: Int) {
            mPosition = position
            setData(getItem(position))
        }

        protected abstract fun setData(model: T)
    }

    inner class LoaderViewHolder(view: DummyListLoaderBinding) : RecyclerView.ViewHolder(view.root)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BaseAdapter<*, *, *>.BaseViewHolder) holder.onBind(position)
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).isLoaderModel) return 1
        return super.getItemViewType(position)
    }
}