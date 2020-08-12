package ru.icames.store.util

import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import ru.icames.store.application.App
import ru.icames.store.data.ProgressListener
import ru.icames.store.domain.model.AppUpdateInfo
import ru.icames.store.domain.use_case.DownloadUseCase
import ru.icames.store.presentation.app_update.AppUpdateManager
import java.io.*
import javax.inject.Inject


class DownloadManager constructor(private val listener: DownloadListener) {
    private var downloadFileTask: DownloadFileTask? = null

    companion object {

        var percent = MutableLiveData<Long>()

        var downloadProgress = MutableLiveData<String>()

        val progressListener =
                ProgressListener { downloaded, contentLength, _ ->
                    downloadProgress.postValue(
                            "${FormatCommons.formatFileSize(downloaded)} / ${FormatCommons.formatFileSize(
                                contentLength
                            )}")
                    percent.postValue(downloaded * 100 / contentLength)
                }

        private const val FOLDER_NAME = "updates"

        fun getDirPath(): String {
            val directory =  File( App.getContext().getExternalFilesDir(null)!!.absolutePath + File.separator + FOLDER_NAME)
            if(!directory.exists()){
                directory.mkdirs()
            }
            return App.getContext().getExternalFilesDir(null)!!.absolutePath + File.separator + FOLDER_NAME + File.separator
        }
    }

    @Inject
    lateinit var downloadUseCase: DownloadUseCase

    init {
        App.getAppComponent().inject(this)
    }

    fun cancel() {
        downloadUseCase.disposeLast()
        resetProgress()
        listener.onCancelDownload()
    }

    private fun resetProgress(){
        percent.value = 0
        downloadProgress.value = ""
    }

    fun download(appUpdateInfo: AppUpdateInfo) {
        App.log("download")
        resetProgress()
        listener.onStartDownload()
        downloadUseCase.downloadFile(appUpdateInfo.updateFile.file.uid,
                onSuccess = {
                    if(it.body() == null){
                        App.log("empty body")
                        listener.onErrorDownload()
                        return@downloadFile
                    }
                    downloadFileTask = DownloadFileTask(appUpdateInfo)
                    downloadFileTask?.execute(it.body())
                },
                onError = {
                    App.log("on error download")
                    listener.onErrorDownload()
                })
    }

    interface DownloadListener {
        fun onStartDownload()
        fun onFileDownloaded(appUpdateInfo: AppUpdateInfo, path: String)
        fun onCancelDownload()
        fun onErrorDownload()
    }

    fun writeResponseBodyToDisk(body: ResponseBody, appUpdateInfo: AppUpdateInfo): Boolean {
        return try {
            val file = File(getDirPath() + AppUpdateManager.UPDATE_FILENAME)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()

                }
                outputStream.flush()
                listener.onFileDownloaded(appUpdateInfo, file.absolutePath)
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }


    private inner class DownloadFileTask(val appUpdateInfo: AppUpdateInfo) : AsyncTask<ResponseBody, Pair<Long, Long>, Boolean>() {


        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (!result) {
                listener.onErrorDownload()
            }

        }

        override fun doInBackground(vararg params: ResponseBody): Boolean {
            return writeResponseBodyToDisk(params[0], appUpdateInfo)
        }
    }


}