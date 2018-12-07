package com.gemini.energy.presentation.util

class Utils {

    companion object {

        fun now() = (System.currentTimeMillis() / 1000.0)
        fun intNow() = intNow(1)
        fun intNow(scale: Int) = (now() * scale).toLong()

    }


}