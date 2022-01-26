package com.kirtan.baseLibrary.base.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.kirtan.baseLibrary.R
import com.kirtan.baseLibrary.utils.toast

abstract class BaseActivity<Screen : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var screen: Screen
    protected open val tag = "BaseActivity"

    @get:LayoutRes
    abstract val getLayout: Int

    val bundle: Bundle get() = intent?.extras ?: Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this::screen.isInitialized.not()) {
            screen = DataBindingUtil.setContentView(this, getLayout)
        }
        screen.lifecycleOwner = this
        if (!bundleFromPreviousActivity(bundle)) {
            toast(getString(R.string.invalid_launch))
            finish()
            return
        }
    }

    protected open fun bundleFromPreviousActivity(bundle: Bundle): Boolean = true
}