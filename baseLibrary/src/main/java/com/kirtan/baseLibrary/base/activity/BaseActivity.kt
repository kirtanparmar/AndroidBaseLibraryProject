package com.kirtan.baseLibrary.base.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.kirtan.baseLibrary.R
import com.kirtan.baseLibrary.utils.toast
import timber.log.Timber

abstract class BaseActivity<Screen : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var screen: Screen
    protected open val tag = "BaseActivity"

    @get:LayoutRes
    abstract val getLayout: Int

    val bundle: Bundle get() = intent?.extras ?: Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d(tag, "onCreated: ")
        screen = DataBindingUtil.setContentView(this, getLayout)
        screen.lifecycleOwner = this
        Timber.d(tag, "onCreated: checking bundle values")
        if (!storeBundleValueIfNeeded(bundle)) {
            Timber.d("onCreate: bundle values are invalid.")
            toast(getString(R.string.invalid_launch))
            finish()
            return
        }
    }

    protected open fun storeBundleValueIfNeeded(bundle: Bundle): Boolean = true
}