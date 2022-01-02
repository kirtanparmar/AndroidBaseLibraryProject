package com.kirtan.baseLibrary.base.activity.listActivity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kirtan.baseLibrary.R
import com.kirtan.baseLibrary.base.ListScreen
import com.kirtan.baseLibrary.base.activity.BaseActivity
import com.kirtan.baseLibrary.base.dataHolder.BaseArrayList
import com.kirtan.baseLibrary.base.dataHolder.BaseObject
import com.kirtan.baseLibrary.base.dataHolder.Operation
import com.kirtan.baseLibrary.utils.gone
import com.kirtan.baseLibrary.utils.show
import com.kirtan.baseLibrary.utils.toast
import timber.log.Timber

abstract class BaseListActivity<Screen : ViewDataBinding, ModelType : BaseObject> :
    BaseActivity<Screen>(), ListScreen<ModelType> {
    override val tag: String
        get() = "BaseListActivity"
    private val listViewModel: ListViewModel<ModelType> by viewModels()
    private val viewModelList get() = listViewModel.models
    private var copyToViewModelList: Boolean
        get() = listViewModel.copyToViewModelList
        set(value) {
            listViewModel.copyToViewModelList = value
        }

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
            if (copyToViewModelList) {
                viewModelList.clear()
            }
            adapter.notifyItemRangeRemoved(0, lastListSize)
            callBack.invoke(Operation())
        }

        override fun emptyListAdded(callBack: (operation: Operation) -> Unit) = checkEmpty(callBack)

        override fun itemRemovedAt(position: Int, callBack: (operation: Operation) -> Unit) {
            if (copyToViewModelList) {
                viewModelList.removeAt(position)
            }
            adapter.notifyItemRemoved(position)
            checkEmpty(callBack)
        }

        override fun itemRemovedUnknownPosition(
            element: ModelType,
            callBack: (operation: Operation) -> Unit
        ) {
            if (copyToViewModelList) {
                viewModelList.remove(element)
            }
            checkEmpty(callBack)
        }

        fun checkEmpty(callBack: (operation: Operation) -> Unit) {
            if (copyToViewModelList) {
                if (isEmpty()) {
                    showErrorOnDisplay(getString(R.string.no_data_found))
                }
            }
            callBack.invoke(Operation())
        }

        override fun newItemAdded(
            position: Int,
            callBack: (operation: Operation) -> Unit
        ) {
            if (copyToViewModelList) {
                try {
                    viewModelList.add(position, get(position))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            adapter.notifyItemInserted(position)
            getErrorTextView()?.gone()
            callBack.invoke(Operation())
        }

        override fun newItemRangeAdded(
            start: Int,
            rangeSize: Int,
            callBack: (operation: Operation) -> Unit
        ) {
            if (copyToViewModelList) {
                viewModelList.addAll(
                    start,
                    subList(fromIndex = start, toIndex = start + rangeSize)
                )
            }
            adapter.notifyItemRangeInserted(start, rangeSize)
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
        Timber.d(tag, "onCreate: ")
        setRv()
        setSwipeRefresh()
    }

    /**
     * RecyclerView setup
     */
    private fun setRv() {
        Timber.d(tag, "setRv: ")
        getRecyclerView().layoutManager = layoutManager
        getRecyclerView().adapter = adapter
        copyToViewModelList = false
        models.clear()
        models.addAll(viewModelList)
        copyToViewModelList = true
        adapter.submitList(models)
    }

    /**
     * Function will show the center progress bar.
     * You can call this function if you have passed center aligned progressbar in getCenterProgressBar function.
     */
    protected open fun showPageProgress() {
        Timber.d(tag, "showPageProgress: ")
        getCenterProgressBar()?.show()
    }

    /**
     * Function will hide the center progress bar.
     * You can call this function if you have passed center aligned progressbar in getCenterProgressBar function.
     */
    protected open fun gonePageProgress() {
        Timber.d(tag, "gonePageProgress: ")
        getCenterProgressBar()?.gone()
    }

    /**
     * To show the error message on the display if you want.
     */
    protected open fun showErrorOnDisplay(text: String) {
        Timber.d(tag, "showErrorOnDisplay: ")
        if (text.isBlank()) {
            Timber.d(tag, "showErrorOnDisplay: error is empty.")
            return
        }
        if (getErrorTextView() == null) {
            Timber.d(tag, "showErrorOnDisplay: error textview is null, showing error toast.")
            toast(text)
        } else getErrorTextView()?.apply {
            setText(text)
            show()
        }
    }

    /**
     * To hide the error message on the display if you want.
     */
    protected open fun hideErrorOnDisplay() {
        Timber.d(tag, "hideErrorOnDisplay: ")
        getErrorTextView()?.gone()
    }

    /**
     * Set the swipe refresh layout you can override this function if you want to do some more task.
     */
    private fun setSwipeRefresh() {
        Timber.d(tag, "setSwipeRefresh: ")
        getSwipeRefreshLayout()?.setOnRefreshListener {
            Timber.d(tag, "setOnRefreshListener: refreshing page.")
            hideErrorOnDisplay()
            models.clear()
            onSwipeRefreshDoExtra()
            setRv()
        }
    }
}