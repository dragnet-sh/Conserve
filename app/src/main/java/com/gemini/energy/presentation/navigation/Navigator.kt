package com.gemini.energy.presentation.navigation

import android.app.Activity
import android.content.Context
import android.widget.Toast

class Navigator constructor(val context: Context) {

    fun message(txt: String) {
        Toast.makeText(context, txt, Toast.LENGTH_SHORT).show()
    }

}
