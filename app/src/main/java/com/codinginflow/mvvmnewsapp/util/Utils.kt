package com.codinginflow.mvvmnewsapp.util

import android.view.View

fun View.showIfOrInvisible(condition: Boolean) {
    visibility = if (condition) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}
