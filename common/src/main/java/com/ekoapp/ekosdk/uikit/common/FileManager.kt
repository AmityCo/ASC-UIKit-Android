package com.ekoapp.ekosdk.uikit.common

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.ekoapp.ekosdk.internal.api.http.EkoOkHttp
import com.ekoapp.ekosdk.uikit.utils.FileDownloadStatus
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2core.Func
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import kotlin.concurrent.thread


class FileManager {
    companion object {
        private lateinit var fetch: Fetch
        private var mPendingIntent: PendingIntent? = null
        private lateinit var mContext: Context

        fun saveFile(mContext: Context, url: String, fileName: String, mimeType: String) {
            this.mContext = mContext
            if (url.isNotEmptyOrBlank() && fileName.isNotEmptyOrBlank()) {

                val dirPath = getFilePath(mContext)
                val filePath = "$dirPath/$fileName"
                val client = EkoOkHttp.newBuilder().build()

                val fetchConfiguration = FetchConfiguration.Builder(mContext)
                        .setDownloadConcurrentLimit(10)
                        .enableLogging(true)
                        .setHttpDownloader(
                                OkHttpDownloader(
                                        client,
                                        Downloader.FileDownloaderType.PARALLEL
                                )
                        )
                        .enableRetryOnNetworkGain(true)
                        .setNotificationManager(object : DefaultFetchNotificationManager(mContext) {

                            override fun getFetchInstanceForNamespace(namespace: String): Fetch {
                                return Fetch.getDefaultInstance()
                            }

                            override fun shouldUpdateNotification(downloadNotification: DownloadNotification): Boolean {
                                if (downloadNotification.status == Status.CANCELLED) {
                                    return true
                                } else {
                                    return super.shouldUpdateNotification(downloadNotification)
                                }
                            }

                            override fun updateNotification(
                                    notificationBuilder: NotificationCompat.Builder,
                                    downloadNotification: DownloadNotification,
                                    context: Context
                            ) {
                                super.updateNotification(
                                        notificationBuilder,
                                        downloadNotification,
                                        context
                                )

                                notificationBuilder
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setContentTitle(downloadNotification.title)
                                        .setOngoing(downloadNotification.isOnGoingNotification)
                                        .setOnlyAlertOnce(true)
                                        .setAutoCancel(true)
                                        .clearActions()

                                if (downloadNotification.isCompleted) {
                                    notificationBuilder.setContentText("Download complete.")
                                } else if (downloadNotification.isDownloading)
                                    Toast.makeText(
                                            context,
                                            "Downloading file...",
                                            Toast.LENGTH_SHORT
                                    ).show()
                                else {
                                    // do nothing
                                }

                                if (downloadNotification.isFailed || downloadNotification.isCompleted) {
                                    notificationBuilder.setProgress(0, 0, false)
                                } else {
                                    val progressIndeterminate =
                                            downloadNotification.progressIndeterminate
                                    val maxProgress =
                                            if (downloadNotification.progressIndeterminate) 0 else 100
                                    val progress =
                                            if (downloadNotification.progress < 0) 0
                                            else downloadNotification.progress
                                    notificationBuilder.setProgress(
                                            maxProgress,
                                            progress,
                                            progressIndeterminate
                                    )
                                }

                                val file = File(filePath)
                                val uri = if (isAndroidQAndAbove()) {
                                    FileProvider.getUriForFile(
                                            context,
                                            context.packageName + ".UikitCommonProvider",
                                            file
                                    )
                                } else {
                                    Uri.fromFile(file)
                                }
                                val intent = Intent(Intent.ACTION_GET_CONTENT)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                intent.setDataAndType(uri, mimeType)

                                mPendingIntent = PendingIntent.getActivity(
                                        mContext,
                                        0,
                                        intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                notificationBuilder.setContentIntent(mPendingIntent)
                            }
                        })
                        .build()

                val request = Request(url, filePath)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL
                request.enqueueAction = EnqueueAction.REPLACE_EXISTING

                fetch = Fetch.getInstance(fetchConfiguration)
                fetch.addListener(object : FetchListener {
                    override fun onAdded(download: Download) {

                    }

                    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {

                    }

                    override fun onWaitingNetwork(download: Download) {

                    }

                    override fun onCompleted(download: Download) {
                        thread {
                            try {
                                if (download.status == Status.COMPLETED) {
                                    if (isAndroidQAndAbove()) {
                                        val values = ContentValues()
                                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                        values.put(
                                                MediaStore.MediaColumns.RELATIVE_PATH,
                                                Environment.DIRECTORY_DOWNLOADS
                                        )
                                        val fileUri = mContext.contentResolver.insert(
                                                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                                        )
                                        if (fileUri != null) {
                                            mContext.contentResolver.openOutputStream(fileUri)
                                                    ?.use { outputStream ->
                                                        val bos = BufferedOutputStream(outputStream)
                                                        val bytes = download.total.toInt()
                                                        val buffer = BufferedInputStream(
                                                                FileInputStream(
                                                                        File(download.file)
                                                                )
                                                        )
                                                        bos.write(buffer.readBytes(), 0, bytes)
                                                        bos.flush()
                                                        bos.close()
                                                    }
                                            values.clear()
                                            values.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                                            mContext.contentResolver.update(
                                                    fileUri,
                                                    values,
                                                    null,
                                                    null
                                            )
                                        }
                                    }
                                    if (!fetch.isClosed) {
                                        fetch.close()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onError(download: Download, error: Error, throwable: Throwable?) {
                        if (!fetch.isClosed) {
                            fetch.close()
                        }
                    }

                    override fun onDownloadBlockUpdated(
                            download: Download,
                            downloadBlock: DownloadBlock,
                            totalBlocks: Int
                    ) {

                    }

                    override fun onStarted(
                            download: Download,
                            downloadBlocks: List<DownloadBlock>,
                            totalBlocks: Int
                    ) {

                    }

                    override fun onProgress(
                            download: Download,
                            etaInMilliSeconds: Long,
                            downloadedBytesPerSecond: Long
                    ) {

                    }

                    override fun onPaused(download: Download) {

                    }

                    override fun onResumed(download: Download) {

                    }

                    override fun onCancelled(download: Download) {

                    }

                    override fun onRemoved(download: Download) {

                    }

                    override fun onDeleted(download: Download) {

                    }
                })

                fetch.enqueue(request, Func { result ->

                }, Func<Error> { })

            }
        }

        private fun getFilePath(context: Context): String {
            return if (isAndroidQAndAbove()) {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .toString()
            }
        }

        private fun isAndroidQAndAbove(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }

        /**
         * Not using now will be used when we'll start downloading Audio Files
         * @author sumitlakra
         * @date 12/01/2020
         */
        fun downloadAudioFile(context: Context, url: String, listener: FileDownloadStatus) {
            val file = getAudioFile(context, url)
            if (file.exists()) {
                listener.onDownloadComplete(Uri.fromFile(file))
                return
            } else {
                val client = EkoOkHttp.newBuilder().build()

                val fetchConfiguration = FetchConfiguration.Builder(context)
                        .setDownloadConcurrentLimit(10)
                        .enableLogging(true)
                        .setHttpDownloader(OkHttpDownloader(client))
                        .build()
                val request = Request(url, file.absolutePath)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL

                val fetch = Fetch.getInstance(fetchConfiguration)
                fetch.addListener(object : FetchListener {
                    override fun onAdded(download: Download) {
                    }

                    override fun onCancelled(download: Download) {
                    }

                    override fun onCompleted(download: Download) {
                        if (download.status == Status.COMPLETED) {
                            if (isAndroidQAndAbove()) {
                                val values = ContentValues().apply {
                                    put(MediaStore.Files.FileColumns.DISPLAY_NAME, file.name)
                                    put(MediaStore.Files.FileColumns.MIME_TYPE, ".mp3")
                                    put(
                                            MediaStore.Files.FileColumns.RELATIVE_PATH,
                                            Environment.DIRECTORY_DOWNLOADS
                                    )
                                    put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                                }
                                val resolver = context.contentResolver
                                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            }
                            if (!fetch.isClosed) {
                                fetch.close()
                            }
                            listener.onDownloadComplete(download.fileUri)
                        }
                    }

                    override fun onDeleted(download: Download) {
                    }

                    override fun onDownloadBlockUpdated(
                            download: Download,
                            downloadBlock: DownloadBlock,
                            totalBlocks: Int
                    ) {
                    }

                    override fun onError(download: Download, error: Error, throwable: Throwable?) {
                        if (!fetch.isClosed) {
                            fetch.close()
                        }
                        listener.onError(error.name)
                    }

                    override fun onPaused(download: Download) {
                    }

                    override fun onProgress(
                            download: Download,
                            etaInMilliSeconds: Long,
                            downloadedBytesPerSecond: Long
                    ) {
                        listener.onProgressUpdate(download.progress)
                    }

                    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                    }

                    override fun onRemoved(download: Download) {
                    }

                    override fun onResumed(download: Download) {
                    }

                    override fun onStarted(
                            download: Download,
                            downloadBlocks: List<DownloadBlock>,
                            totalBlocks: Int
                    ) {
                    }

                    override fun onWaitingNetwork(download: Download) {
                    }

                })

                fetch.enqueue(request)
            }
        }

        fun getAudioFile(context: Context, url: String): File {
            val uri = Uri.parse(url)
            val fileName = "Audio_${uri.pathSegments[3]}.mp3"
            val filePath = "${getFilePath(context)}/$fileName"
            return File(filePath)
        }
    }
}