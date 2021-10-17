package com.kirtan.mylibrary.base.activity.listActivity

import androidx.lifecycle.ViewModel
import com.kirtan.mylibrary.base.dataHolder.BaseObject

class ListViewModel<T : BaseObject> : ViewModel() {
    var copyToViewModelList: Boolean = true
    val models: ArrayList<T> = ArrayList()
}