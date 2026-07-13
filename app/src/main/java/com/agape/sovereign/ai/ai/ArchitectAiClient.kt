package com.agape.sovereign.ai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * ArchitectAI — local LLM client powered by gemma4:e2b via Ollama.
 *
 * - Model  : gemma4:e2b
 * - Tokens : -1 (unlimited — Ollama honours num_predict = -1)
 * - Base URL: configurable; defaults to device localhost (127.0.0.1:11434)
 *             or an Android-emulator-reachable host (10.0.2.2:11434).
 *
 * For web-app / PWA usage the same endpoint is reachable from a WebView
 * pointed at the Ollama HTTP API.
 *
 * Usage:
 *   val reply = ArchitectAiClient.ask("Is this app safe?")
 */
object ArchitectAiClient {

    const val MODEL = "gemma4:e2b"
    const val AI_NAME = "Architect AI"

    /**
     * Ollama base URL.  On a physical device running Ollama on the same
     * machine reachable via USB/Wi-Fi replace with the machine's LAN IP.
     * For the Android emulator 10.0.2.2 maps to the host loopback.
     */
    var baseUrl: String = "http://10.0.2.2:11434"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)   // local models can be slow on first token
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * System prompt injected before every user message.
     * Scoped to privacy and mobile-security analysis — keeps the model
     * on-topic and safe for offline processing.
     */
    private const val SYSTEM_PROMPT = """You are $AI_NAME, an expert AI assistant embedded inside the Aegis Privacy app.
Your role is to analyze privacy risks, mobile security threats, data exposure patterns, and provide actionable hardening recommendations.
You run entirely on-device — no data leaves the phone.
Be concise, direct, and technical. Respond in plain text only (no markdown)."""

    /**
     * Send [userMessage] to the local gemma4:e2b model and return the
     * assistant's reply string, or an error message prefixed with "ERROR:".
     *
     * [maxTokens] = -1 means unlimited (Ollama default behaviour).
     */
    suspend fun ask(userMessage: String, maxTokens: Int = -1): String =
        withContext(Dispatchers.IO) {
            try {
                val bodyJson = JSONObject().apply {
                    put("model", MODEL)
                    put("stream", false)
                    put("options", JSONObject().apply {
                        put("num_predict", maxTokens) // -1 = unlimited
                        put("temperature", 0.7)
                    })
                    put("messages", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", SYSTEM_PROMPT)
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", userMessage)
                        })
                    })
                }

                val request = Request.Builder()
                    .url("$baseUrl/api/chat")
                    .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext "ERROR: HTTP ${response.code} — ${response.message}"
                }

                val rawBody = response.body?.string()
                    ?: return@withContext "ERROR: Empty response from $AI_NAME"

                val root = JSONObject(rawBody)
                root.getJSONObject("message").getString("content").trim()

            } catch (e: Exception) {
                "ERROR: $AI_NAME is offline or unreachable (${e.message}). " +
                "Make sure Ollama is running with: ollama run $MODEL"
            }
        }

    /**
     * Convenience wrapper: analyse a privacy/security question and return
     * the verdict as a single string.  Always uses -1 (unlimited tokens).
     */
    suspend fun analyzePrivacySecurity(context: String, question: String): String =
        ask("Context: $context\n\nQuestion: $question")

    /**
     * Generate a hardening recommendation for a given security finding.
     */
    suspend fun recommendFix(finding: String): String =
        ask("Security finding: $finding\n\nProvide a specific, actionable fix recommendation.")
}
