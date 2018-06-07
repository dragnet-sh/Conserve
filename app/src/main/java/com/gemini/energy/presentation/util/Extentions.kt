package com.gemini.energy.presentation.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/*
* Hide Input Keyboard
* */

fun View.hideInput() {

    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromInputMethod(windowToken, 0)

}
