package com.kirtan.mylibrary.base.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.utils.toast

/**
 * Extend this class with your activity class for using the library features.
 */
abstract class BaseActivity<Screen : ViewDataBinding> : AppCompatActivity() {
    /**
     * You can access the screen object through out the activity anywhere you want.
     * ie: screen.textViewId.text = "Hello world"
     */
    protected lateinit var screen: Screen

    /**
     * This value should contain the layout file.
     */
    @get:LayoutRes
    abstract val getLayout: Int

    /**
     * Value holds the intent bundle
     */
    val bundle: Bundle get() = intent?.extras ?: Bundle()

    /**
     * This method should be overridden whenever you needed to set up screen though code.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = DataBindingUtil.setContentView(this, getLayout)
        screen.lifecycleOwner = this
        if (!storeBundleValueIfNeeded(bundle)) {
            toast(getString(R.string.invalid_launch))
            finish()
            return
        }
    }

    /**
     * @param bundle contains the data passed from another @activity.
     * @return true if the bundle value is valid. You have to pass true if there is no need for the bundle in this activity.
     */
    protected abstract fun storeBundleValueIfNeeded(bundle: Bundle): Boolean
}