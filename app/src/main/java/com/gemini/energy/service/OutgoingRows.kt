package com.gemini.energy.service

import android.content.Context
import android.os.Environment
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


    /**
     * Utility Methods
     * */
    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    fun parseFilenameFromPath(filePath: String): String {
        val index = filePath.lastIndexOf('/') + 1
        return filePath.substring(index)
    }


    private fun getDocumentFolderPath(subFolderPath: String? = null): File? {

        val folderDir: File?

        if (isExternalStorageWritable()) {
            Log.d(TAG, "External - Storage :: Writable")
            if (subFolderPath == null) {
                folderDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).path)
            } else {
                folderDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).path + "/$subFolderPath")
            }
        } else {
            Log.d(TAG, "External - Storage :: Not Writable")
            if (subFolderPath == null) {
                folderDir = File(context.filesDir.path + "/Documents")
            } else {
                folderDir = File(context.filesDir.path + "/Documents/$subFolderPath")
            }
        }

        if (!folderDir.isDirectory) {
            Log.d(TAG, "****** Creating Directory *******")
            Log.d(TAG, folderDir.toString())

            folderDir.mkdirs()
        }

        return folderDir
    }


    fun setFilePath(path: String, fileName: String) {
        val directory = getDocumentFolderPath("gemini/$path")!!
        this.filePath = File(directory.toString(), fileName)

        if (this.filePath.exists()) {
            Log.d(TAG, "File Exists")
        } else {
            Log.d(TAG, "File Does Not Exists")
        }
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