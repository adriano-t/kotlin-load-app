package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val filename = intent.getStringExtra("filename")
        if (!filename.isNullOrEmpty()) {
            binding.contentDetail.textFilenameValue.text = filename
        }

        val status = intent.getStringExtra("status")
        if (!status.isNullOrEmpty()) {
            val textStatus = binding.contentDetail.textStatusValue
            textStatus.text = status
            textStatus.setTextColor(when (status) {
                getString(R.string.success) -> resources.getColor(R.color.green, null)
                getString(R.string.fail) -> resources.getColor(R.color.red, null)
                else -> resources.getColor(R.color.red, null)
            })
        }

        binding.contentDetail.buttonOk.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
            //close current activity
            finish()
        }
    }
}
