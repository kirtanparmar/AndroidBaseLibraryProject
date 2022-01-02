package com.kirtan.baseLibrary.base.activity.formActivity

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.google.android.material.button.MaterialButton
import com.kirtan.baseLibrary.base.activity.BaseActivity

abstract class FormActivity<Screen : ViewDataBinding> : BaseActivity<Screen>() {
    abstract val submitButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        submitButton.setOnClickListener { if (isValidFrom()) submitForm() }
    }

    abstract fun isValidFrom(): Boolean

    abstract fun submitForm()
}