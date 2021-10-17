package com.kirtan.mylibrary.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.activity.BaseActivity

var alertDialogActivity: AlertDialog? = null

fun BaseActivity<*>.showMessage(
    title: CharSequence? = null,
    message: CharSequence?,
    cancelable: Boolean = false,
    icon: Drawable? = null,
    positiveButton: CharSequence = getString(R.string.ok),
    positiveButtonListener: DialogInterface.OnClickListener? = DialogInterface.OnClickListener { _, _ -> },
    showNegativeButton: Boolean = false,
    negativeButton: CharSequence = getString(R.string.cancel),
    negativeButtonListener: DialogInterface.OnClickListener? = DialogInterface.OnClickListener { _, _ -> },
    showNeutralButton: Boolean = false,
    neutralButton: CharSequence = getString(R.string.cancel),
    neutralButtonListener: DialogInterface.OnClickListener? = DialogInterface.OnClickListener { _, _ -> }
): AlertDialog {
    alertDialogActivity?.dismiss()
    alertDialogActivity = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(
            if (message?.isNotEmpty() == true) {
                message
            } else {
                getString(R.string.something_went_wrong)
            }
        )
        .setCancelable(cancelable)
        .setIcon(icon)
        .setPositiveButton(positiveButton, positiveButtonListener)
        .apply {
            if (showNegativeButton) {
                setNegativeButton(negativeButton, negativeButtonListener)
            }
            if (showNeutralButton) {
                setNeutralButton(neutralButton, neutralButtonListener)
            }
        }
        .show()
    return alertDialogActivity!!
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
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