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

    abstract val layoutManager: RecyclerView.LayoutManager
    abstract val emptyObjectForNullAssertion: ModelType

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

    protected val adapter: ListAdapter<ModelType, *> by lazy { createAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRv()
        setSwipeRefresh()
    }

    private fun setRv() {
        getRecyclerView().layoutManager = layoutManager
        getRecyclerView().adapter = adapter
        copyToViewModelList = false
        models.clear()
        models.addAll(viewModelList)
        copyToViewModelList = true
        adapter.submitList(models)
    }

    protected open fun showPageProgress() {
        getCenterProgressBar()?.show()
    }

    protected open fun gonePageProgress() {
        getCenterProgressBar()?.gone()
    }

    protected open fun showErrorOnDisplay(text: String) {
        if (text.isBlank()) {
            return
        }
        if (getErrorTextView() == null) {
            toast(text)
        } else getErrorTextView()?.apply {
            setText(text)
            show()
        }
    }

    protected open fun hideErrorOnDisplay() {
        getErrorTextView()?.gone()
    }

    private fun setSwipeRefresh() {
        getSwipeRefreshLayout()?.setOnRefreshListener {
            hideErrorOnDisplay()
            models.clear()
            onSwipeRefreshDoExtra()
            setRv()
        }
    }
}