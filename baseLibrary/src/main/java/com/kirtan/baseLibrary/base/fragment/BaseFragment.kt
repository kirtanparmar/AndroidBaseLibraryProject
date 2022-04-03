package com.kirtan.baseLibrary.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<Screen : ViewDataBinding> : Fragment() {
    protected lateinit var screen: Screen
    protected open val screenTag = "BaseFragment"

    @get:LayoutRes
    abstract val getLayout: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (!this::screen.isInitialized) {
            screen = DataBindingUtil.inflate(inflater, getLayout, container, false)
        }
        return screen.root
    }
}