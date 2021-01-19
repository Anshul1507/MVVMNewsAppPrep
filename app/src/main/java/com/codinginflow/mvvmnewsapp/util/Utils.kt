package com.codinginflow.mvvmnewsapp.util

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun View.showIfOrInvisible(condition: Boolean) {
    visibility = if (condition) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG, view: View = requireView()) {
    Snackbar.make(view, message, duration).show()
}