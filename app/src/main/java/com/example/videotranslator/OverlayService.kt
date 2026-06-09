package com.example.videotranslator

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var container: LinearLayout
    private lateinit var downloader: VideoDownloader
    private lateinit var translator: GeminiTranslator
    private lateinit var tts: SmartTTS
    private val scope = CoroutineScope(Dispatchers.Main)
    private var currentSpeed = 1.0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        downloader = VideoDownloader(this)
        translator = GeminiTranslator("AQ.Ab8RN6LCcW8mp0DMHPJzLN1czBsDLw9GYjVV3KCLi1tg4xYspw") 
        tts = SmartTTS(this) {}
        buildOverlayLayout()
    }

    private fun buildOverlayLayout() {
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#DD000000"))
            setPadding(30, 30, 30, 30)
        }

        val actionButton = Button(this).apply {
            text = "Перевести из буфера"
            setBackgroundColor(Color.parseColor("#FF5722"))
        }

        val speedLabel = TextView(this).apply {
            text = "Скорость речи: 1.0x"
            setTextColor(Color.WHITE)
        }

        val speedSeekBar = SeekBar(this).apply {
            max = 20
            progress = 5
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) {
                    currentSpeed = 0.5f + (p / 10f)
                    speedLabel.text = "Скорость: ${String.format("%.1f", currentSpeed)}x"
                    tts.applySettings(currentSpeed, 1.0f)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }

        actionButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val url = clipData.getItemAt(0).text.toString()
                if (url.contains("tiktok") || url.contains("youtube") || url.contains("youtu.be")) {
                    processVideo(url, actionButton)
                } else {
                    Toast.makeText(this, "В буфере нет ссылки на видео", Toast.LENGTH_SHORT).show()
                }
            }
        }

        container.addView(actionButton)
        container.addView(speedLabel)
        container.addView(speedSeekBar)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM }

        windowManager.addView(container, params)
    }

private fun processVideo(url: String, button: Button) {
    AppLogger.info("Начата обработка ссылки: $url")
    button.text = "Скачивание аудио..."
    button.isEnabled = false
    
    scope.launch {
        val file = downloader.downloadAudio(url)
        if (file != null) {
            AppLogger.debug("Аудио успешно скачано, размер: ${file.length()} байт")
            button.text = "Gemini ИИ думает..."
            
            val resultText = translator.processAudio(file)
            AppLogger.debug("Получен ответ от Gemini: ${resultText.take(50)}...") // Логируем первые 50 символов
            
            button.text = "Озвучка..."
            tts.applySettings(currentSpeed, 1.0f)
            tts.speak(resultText)
            button.text = "Готово!"
        } else {
            AppLogger.error("Не удалось скачать аудио по ссылке: $url")
            button.text = "Ошибка загрузки"
        }
        button.isEnabled = true
    }
}

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        if (::container.isInitialized) windowManager.removeView(container)
    }
}
