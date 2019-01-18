package com.gemini.energy.presentation.audit

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Parcelable
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.http.OkHttp3Requestor
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.WriteMode
import com.dropbox.core.v2.users.FullAccount
import com.gemini.energy.App
import kotlinx.android.parcel.Parcelize
import timber.log.Timber
import java.io.ByteArrayInputStream

@Parcelize
class DropBox: Parcelable {

    companion object {

        fun captureAuthToken() {
            val prefs: SharedPreferences = App.instance.getSharedPreferences("dropbox", Context.MODE_PRIVATE)
            var accessToken = prefs.getString("access-token", null)
            if (accessToken == null) {
                accessToken = Auth.getOAuth2Token()
                if (accessToken != null) {
                    prefs.edit().putString("access-token", accessToken).apply()
                    init(accessToken)
                }
            } else {
                init(accessToken)
            }
        }

        private fun init(accessToken: String) {
            DropBoxClientFactory.init(accessToken)
        }

        fun hasToken(): Boolean {
            val prefs: SharedPreferences = App.instance.getSharedPreferences("dropbox", Context.MODE_PRIVATE)
            val accessToken = prefs.getString("access-token", null)
            Timber.d("*********** DROP BOX ACCESS TOKEN ************")
            Timber.d(accessToken)
            return accessToken != null
        }

        fun clearAuthToken() {
            Timber.d("Clearing Auth Token")
            val prefs: SharedPreferences = App.instance.getSharedPreferences("dropbox", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.remove("access-token")
            editor.apply()
        }

        fun getClient() = DropBoxClientFactory.getClient()

    }


    class DropBoxClientFactory {
        companion object {
            private var sDbxClient: DbxClientV2? = null

            fun init(accessToken: String) {
                if (sDbxClient == null) {
                    val requestConfig: DbxRequestConfig = DbxRequestConfig.newBuilder("gemini-energy")
                            .withHttpRequestor(OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                            .build()

                    sDbxClient = DbxClientV2(requestConfig, accessToken)
                }
            }

            fun getClient(): DbxClientV2 {
                if (sDbxClient == null) {
                    throw IllegalStateException("Client not initialized.")
                }
                return sDbxClient!!
            }

        }
    }
}

class GetCurrentAccountTask(
        private val mDbxClientV2: DbxClientV2,
        private val mCallback: Callback): AsyncTask<Unit, Unit, FullAccount>() {

    private var mException: Exception? = null

    interface Callback {
        fun onComplete(result: FullAccount?)
        fun onError(e: Exception?)
    }

    override fun doInBackground(vararg params: Unit?): FullAccount? {
        try {
            return mDbxClientV2.users().currentAccount
        } catch (e: Exception) { mException = e }

        return null
    }

    override fun onPostExecute(result: FullAccount?) {
        super.onPostExecute(result)
        if (mException != null) {
            mCallback.onError(mException)
        } else {
            mCallback.onComplete(result)
        }
    }
}

class UploadFileTask(
        private val mDbxClient: DbxClientV2,
        private val mCallBack: Callback): AsyncTask<String, Unit, FileMetadata?>() {

    interface Callback {
        fun onUploadComplete(result: FileMetadata)
        fun onError(e: Exception?)
    }

    private var mException: Exception? = null

    override fun doInBackground(vararg params: String?): FileMetadata? {
        val data: String
        val path: String
        
        if (params.count() == 2) {
            data = params[0] as String
            path = params[1] as String
        } else { return null }

        try {
            ByteArrayInputStream(data.toByteArray()).use {
                return mDbxClient.files().uploadBuilder(path)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(it)
            }
        } catch (e: Exception) {
            mException = e
        }

        return null
    }

    override fun onPostExecute(result: FileMetadata?) {
        super.onPostExecute(result)
        when {
            mException != null -> mCallBack.onError(mException)
            result == null -> mCallBack.onError(null)
            else -> mCallBack.onUploadComplete(result)
        }
    }
}