package com.kirtan.mylibrary.base.activity

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ListScreen
import com.kirtan.mylibrary.base.dataHolder.BaseArrayList
import com.kirtan.mylibrary.base.dataHolder.Object
import com.kirtan.mylibrary.base.dataHolder.Operation
import com.kirtan.mylibrary.utils.gone
import com.kirtan.mylibrary.utils.show

abstract class MyListActivity<Screen : ViewDataBinding, ModelType : Object> : MyActivity<Screen>(),
    ListScreen<ModelType> {
    /**
     * Layout manager object. You've to override the value as you wish.
     * #Note Only LinearLayoutManager object can be used for paging.
     */
    abstract val layoutManager: RecyclerView.LayoutManager
    abstract val emptyObjectForNullAssertion: ModelType

    /**
     * This list contains the callbacks for the many functions in the arraylist.
     */
    protected val models = object : BaseArrayList<ModelType>() {
        override fun listCleared(lastListSize: Int, callBack: (operation: Operation) -> Unit) {
            adapter.notifyItemRangeRemoved(0, lastListSize)
            callBack.invoke(Operation())
        }

        override fun emptyListAdded(callBack: (operation: Operation) -> Unit) = checkEmpty(callBack)

        override fun itemRemovedAt(position: Int, callBack: (operation: Operation) -> Unit) {
            adapter.notifyItemRemoved(position)
            checkEmpty(callBack)
        }

        override fun itemRemovedUnknownPosition(callBack: (operation: Operation) -> Unit) {
            checkEmpty(callBack)
        }

        fun checkEmpty(callBack: (operation: Operation) -> Unit) {
            if (isEmpty()) {
                showErrorOnDisplay(getString(R.string.no_data_found))
            }
            callBack.invoke(Operation())
        }

        override fun newItemAdded(position: Int, callBack: (operation: Operation) -> Unit) {
            adapter.notifyItemInserted(position)
            getErrorTextView()?.gone()
            callBack.invoke(Operation())
        }

        override fun newItemRangeAdded(
            start: Int,
            end: Int,
            callBack: (operation: Operation) -> Unit
        ) {
            adapter.notifyItemRangeInserted(start, end)
            getErrorTextView()?.gone()
            callBack.invoke(Operation())
        }

        override fun emptyObjectForNullAssertion(): ModelType = emptyObjectForNullAssertion
    }

    /**
     * You can access adapter though out the activity.
     */
    protected val adapter: ListAdapter<ModelType, *> by lazy { createAdapter() }

    /**
     * This function will set the recyclerView and swipeRefreshView if needed.
     * This function should be overridden if you have to do some extra work in this method.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRv()
        setSwipeRefresh()
    }

    /**
     * RecyclerView setup
     */
    private fun setRv() {
        getRecyclerView().layoutManager = layoutManager
        getRecyclerView().adapter = adapter
        adapter.submitList(models)
    }

    /**
     * Function will show the center progress bar.
     * You can call this function if you have passed center aligned progressbar in getCenterProgressBar function.
     */
    protected open fun showPageProgress() {
        getCenterProgressBar()?.show()
    }

    /**
     * Function will hide the center progress bar.
     * You can call this function if you have passed center aligned progressbar in getCenterProgressBar function.
     */
    protected open fun gonePageProgress() {
        getCenterProgressBar()?.gone()
    }

    /**
     * To show the error message on the display if you want.
     */
    protected open fun showErrorOnDisplay(text: String) {
        if (text.isBlank()) return
        getErrorTextView()?.apply {
            setText(text)
            show()
        }
    }

    /**
     * To hide the error message on the display if you want.
     */
    protected open fun hideErrorOnDisplay() {
        getErrorTextView()?.gone()
    }

    /**
     * Set the swipe refresh layout you can override this function if you want to do some more task.
     */
    protected open fun setSwipeRefresh() {
        getSwipeRefreshLayout()?.setOnRefreshListener {
            hideErrorOnDisplay()
            models.clear()
            setRv()
        }
    }
}