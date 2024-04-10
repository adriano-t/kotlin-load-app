package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.loadingButton.setOnClickListener {
            val button = it as LoadingButton
            if(binding.contentMain.choices.checkedRadioButtonId <= 0) {
                Toast.makeText(
                    applicationContext,
                    getText(R.string.choose_rb_element),
                    Toast.LENGTH_SHORT
                ).show()
            } else if(downloadID > 0) {
                button.cancelProgressAnimation()
                downloadID = 0;
            } else {
                val url = when (binding.contentMain.choices.checkedRadioButtonId) {
                    R.id.rb_app -> URL_APP
                    R.id.rb_glide -> URL_GLIDE
                    R.id.rb_retrofit -> URL_RETROFIT
                    else -> ""
                }
                download(url)
                button.startProgressAnimation()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if(downloadID == id) {
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().apply {
                    setFilterById(id)
                }
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (columnIndex != -1) {
                        val status = cursor.getInt(columnIndex)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                downloadID = 0;
                                binding.contentMain.loadingButton.endProgressAnimation()
                                Toast.makeText(applicationContext, "Download success", Toast.LENGTH_SHORT).show()
                            }
                            DownloadManager.STATUS_FAILED -> {
                                downloadID = 0;
                                binding.contentMain.loadingButton.endProgressAnimation()
                                Toast.makeText(applicationContext, "Download failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    cursor.close()
                }
            }
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        downloadID = downloadManager.enqueue(request)
    }

    companion object {
        private const val URL_APP = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }
}