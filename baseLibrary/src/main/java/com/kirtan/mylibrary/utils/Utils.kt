package com.kirtan.mylibrary.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.kirtan.mylibrary.base.activity.BaseActivity

fun View.gone() {
    this.post { this.visibility = View.GONE }
}

fun View.hide() {
    this.post { this.visibility = View.INVISIBLE }
}

fun View.show() {
    this.post { this.visibility = View.VISIBLE }
}

private fun baseToast(context: Context, text: CharSequence) {
    if (text.isBlank()) {
        return
    }
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}

fun BaseActivity<*>.toast(text: CharSequence) {
    baseToast(this, text)
}

fun FragmentActivity.toast(text: CharSequence) {
    baseToast(this, text)
}

fun Fragment.toast(text: CharSequence) {
    requireActivity().toast(text)
}