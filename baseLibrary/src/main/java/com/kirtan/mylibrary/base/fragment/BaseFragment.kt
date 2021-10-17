package com.kirtan.mylibrary.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * Extend this class with your fragment class for using the library features.
 */
abstract class BaseFragment<SCREEN : ViewDataBinding> : Fragment() {
    /**
     * You can access the screen object through out the fragment anywhere you want.
     * ie: screen.textViewId.text = "Hello world"
     */
    protected lateinit var screen: SCREEN

    /**
     * This value should contain the layout file.
     */
    @get:LayoutRes
    abstract val getLayout: Int

    /**
     * This function will automatically initialize screen object.
     * If you still want to override this function, please return the super.onCreateView in the last return statement.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        screen = DataBindingUtil.inflate(inflater, getLayout, container, false)
        return screen.root
    }

    /**
     * Do the needful changes in your views by using #screen object.
     */
    abstract override fun onViewCreated(view: View, savedInstanceState: Bundle?)
}