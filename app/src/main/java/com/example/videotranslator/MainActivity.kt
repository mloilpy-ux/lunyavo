package com.example.videotranslator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val btn = Button(this).apply { text = "Запустить Оверлей Переводчика" }
        setContentView(btn)

        btn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, 123)
            } else {
                startService(Intent(this, OverlayService::class.java))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && Settings.canDrawOverlays(this)) {
            startService(Intent(this, OverlayService::class.java))
            finish()
        } else {
            Toast.makeText(this, "Разрешение на оверлей обязательно!", Toast.LENGTH_SHORT).show()
        }
    }
}
