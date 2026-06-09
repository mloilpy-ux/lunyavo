package com.example.videotranslator

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoDownloader(private val context: Context) {
    init {
        try { YoutubeDL.getInstance().init(context) } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun downloadAudio(videoUrl: String): File? = withContext(Dispatchers.IO) {
        val downloadDir = File(context.cacheDir, "downloads").apply { mkdirs() }
        downloadDir.deleteRecursively()
        downloadDir.mkdirs()

        val request = YoutubeDLRequest(videoUrl).apply {
            addOption("--extract-audio")
            addOption("--audio-format", "mp3")
            addOption("-o", "${downloadDir.absolutePath}/audio.%(ext)s")
        }

        return@withContext try {
            YoutubeDL.getInstance().execute(request, null)
            File(downloadDir, "audio.mp3").takeIf { it.exists() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
