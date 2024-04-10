package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = -1
    private var selectedFile = ""

    val NOTIFICATION_ID = 0
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        initNotifications()
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
                downloadID = -1;
            } else {
                val url = when (binding.contentMain.choices.checkedRadioButtonId) {
                    R.id.rb_app -> URL_APP
                    R.id.rb_glide -> URL_GLIDE
                    R.id.rb_retrofit -> URL_RETROFIT
                    else -> ""
                }
                val radioButton = findViewById<RadioButton>(binding.contentMain.choices.checkedRadioButtonId)
                selectedFile = radioButton.text.toString()
                download(url)
                button.startProgressAnimation()
            }
        }
    }

    private fun initNotifications() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getText(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = applicationContext.getString(R.string.notification_description)
                lightColor = Color.GREEN
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
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


                                sendNotification(selectedFile, getString(R.string.success))

                            }
                            DownloadManager.STATUS_FAILED -> {
                                downloadID = 0;
                                binding.contentMain.loadingButton.endProgressAnimation()
                                Toast.makeText(applicationContext, "Download failed", Toast.LENGTH_SHORT).show()

                                sendNotification(selectedFile, getString(R.string.fail))
                            }
                        }
                    }
                    cursor.close()
                }
            }
        }
    }

    private fun sendNotification(fileName: String, status: String) {

        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra("filename", fileName)
        intent.putExtra("status", status)


        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getText(R.string.notification_title))
            .setContentText(applicationContext.getText(R.string.notification_description))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_assistant_black_24dp, applicationContext.getString(R.string.notification_button), contentIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(NOTIFICATION_ID, builder.build())
            } else {
                Toast.makeText(applicationContext, "Please enable notifications", Toast.LENGTH_SHORT).show()
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
        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/master.zi" //error on purpose to make it fail
        private const val CHANNEL_ID = "channelId"
    }
}