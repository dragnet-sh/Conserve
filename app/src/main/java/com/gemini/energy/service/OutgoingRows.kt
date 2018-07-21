package com.gemini.energy.service

import android.content.Context
import android.util.Log
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

class OutgoingRows(private val context: Context) {

    var header: MutableList<String>? = mutableListOf()
    var rows: MutableList<Map<String, String>>? = mutableListOf()
    lateinit var filePath: File

    fun saveFile(data:String = data()): Boolean {

        try {
            val inputStream = ByteArrayInputStream(data.toByteArray())
            val outputStream = BufferedOutputStream(FileOutputStream(filePath!!))

            var buffer = ByteArray(1024)
            var bytesRead = inputStream.read(buffer, 0, buffer.size)

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer, 0, buffer.size)
            }

            inputStream.close()
            outputStream.close()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun parseFilenameFromPath(filePath: String): String {
        val index = filePath.lastIndexOf('/') + 1
        return filePath.substring(index)
    }

    fun setFilePath(path: String, filename: String) {
        Log.d(TAG, context.filesDir.path)
        Log.d(TAG, path)

        val dir = File(context.filesDir.path + "/Gemini/Energy/$path")
        dir.mkdirs()

        filePath = File(dir, filename)
    }

    fun data(): String {
        val buffer = StringBuilder()
        buffer.append(header?.joinToString())
        buffer.append("\r\n")

        for (row in rows!!) {
            val tmp: MutableList<String> = mutableListOf()
            header?.forEach { item ->
                tmp.add(row[item] ?: "")
            }
            buffer.append(tmp.joinToString())
            buffer.append("\r\n")
        }

        Log.d(TAG, buffer.toString())
        return buffer.toString()
    }

    companion object {
        private const val TAG = "OutgoingRows"
    }

}