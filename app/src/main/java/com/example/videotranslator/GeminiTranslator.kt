package com.example.videotranslator

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class GeminiTranslator(apiKey: String) {
    private val model = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)

    suspend fun processAudio(audioFile: File): String = withContext(Dispatchers.IO) {
        val audioBytes = audioFile.readBytes()
        val prompt = "Ты — продвинутый ИИ-переводчик. Слушай это аудио из видео и переведи его на русский язык. Сделай текст плавным, удобным для чтения вслух. Выведи только перевод."

        return@withContext try {
            val response = model.generateContent(
                content {
                    blob("audio/mp3", audioBytes)
                    text(prompt)
                }
            )
            response.text ?: "Ошибка: пустой ответ от ИИ"
        } catch (e: Exception) {
    AppLogger.error("Сбой при запросе к Gemini API", e)
    "Ошибка перевода: ${e.localizedMessage}"
    }
          }
    }
