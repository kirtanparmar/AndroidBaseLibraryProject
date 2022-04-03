package com.kirtan.baseLibrary.base.activity.listActivity

import androidx.lifecycle.ViewModel
import com.kirtan.baseLibrary.listHelper.dataHolder.BaseObject

class ListViewModel<T : BaseObject> : ViewModel() {
    var copyToViewModelList: Boolean = true
    val models: ArrayList<T> = ArrayList()
}