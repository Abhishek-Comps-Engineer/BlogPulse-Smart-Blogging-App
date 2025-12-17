package com.example.blogpulse.gemini

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {

    private const val BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generate(prompt: String, apiKey: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw IllegalStateException("Gemini API key missing")

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val body = requestJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response from Gemini")

            if (!response.isSuccessful) {
                throw Exception("Gemini API error ${response.code}: $responseBody")
            }

            // Parse response
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
                ?: throw Exception("No candidates returned")

            if (candidates.length() == 0) throw Exception("Empty candidates list")

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
                ?: throw Exception("No content returned")

            val parts = content.optJSONArray("parts")
                ?: throw Exception("No parts returned")

            if (parts.length() == 0) throw Exception("Empty parts list")

            val text = parts.getJSONObject(0).optString("text", "")

            return@withContext text
        }
    }
}
