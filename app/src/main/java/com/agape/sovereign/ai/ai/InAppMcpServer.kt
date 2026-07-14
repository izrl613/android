package com.agape.sovereign.ai.ai

import android.util.Log
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/**
 * InAppMcpServer — An embedded HTTP server running inside the Android app.
 *
 * This allows the Android app to host the MCP endpoints locally on the phone (port 3001),
 * serving as a local bridge to Ollama. Both the app itself and any external/local PWA
 * clients can connect to this server for a fully offline experience.
 */
object InAppMcpServer {
    private const val TAG = "InAppMcpServer"
    private var server: HttpServer? = null
    private val executor = Executors.newFixedThreadPool(4)

    var port: Int = 3001
        private set

    val isRunning: Boolean
        get() = server != null

    /**
     * Start the in-app HTTP server on the specified port.
     */
    fun start(port: Int = 3001) {
        if (server != null) {
            Log.d(TAG, "Server is already running on port ${this.port}")
            return
        }
        this.port = port
        try {
            // Bind to all interfaces (0.0.0.0) so other devices on the same Wi-Fi can connect
            server = HttpServer.create(InetSocketAddress("0.0.0.0", port), 0).apply {
                createContext("/android/health", HealthHandler())
                createContext("/android/ask", AskHandler())
                createContext("/android/analyze_vector", AnalyzeVectorHandler())
                createContext("/android/audit_recommendation", AuditRecommendationHandler())
                setExecutor(this@InAppMcpServer.executor)
                start()
            }
            Log.i(TAG, "In-App MCP Server started successfully on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start In-App MCP Server: ${e.message}", e)
        }
    }

    /**
     * Stop the server and release resources.
     */
    fun stop() {
        server?.let {
            it.stop(0)
            server = null
            Log.i(TAG, "In-App MCP Server stopped")
        }
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private class HealthHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            handleRequest(exchange) {
                val ollamaReachable = runBlocking(Dispatchers.IO) {
                    try {
                        val client = okhttp3.OkHttpClient.Builder()
                            .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                            .build()
                        val request = okhttp3.Request.Builder()
                            .url("${ArchitectAiClient.baseUrl}/api/tags")
                            .build()
                        client.newCall(request).execute().isSuccessful
                    } catch (e: Exception) {
                        false
                    }
                }

                JSONObject().apply {
                    put("status", if (ollamaReachable) "ok" else "degraded")
                    put("server", "in-app-architect-mcp-server")
                    put("version", "1.0.0")
                    put("model", ArchitectAiClient.MODEL)
                    put("ollama", ArchitectAiClient.baseUrl)
                    put("ollamaReachable", ollamaReachable)
                    put("client", "android-in-app")
                    put("timestamp", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date()))
                }
            }
        }
    }

    private class AskHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            handleRequest(exchange) {
                if (exchange.requestMethod != "POST") {
                    throw IllegalArgumentException("Only POST method is supported")
                }
                val body = parseRequestBody(exchange)
                val question = body.optString("question")
                val context = body.optString("context", "")

                if (question.isNullOrEmpty()) {
                    throw IllegalArgumentException("question is required")
                }

                val prompt = if (context.isNotEmpty()) {
                    "Context from the app:\n$context\n\nQuestion: $question"
                } else {
                    question
                }

                val answer = runBlocking(Dispatchers.IO) {
                    ArchitectAiClient.ask(prompt)
                }

                JSONObject().apply {
                    put("answer", answer)
                }
            }
        }
    }

    private class AnalyzeVectorHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            handleRequest(exchange) {
                if (exchange.requestMethod != "POST") {
                    throw IllegalArgumentException("Only POST method is supported")
                }
                val body = parseRequestBody(exchange)
                val vectorId = body.optString("vector_id")
                val vectorName = body.optString("vector_name")
                val rawData = body.optString("raw_data")
                val score = body.optDouble("sovereign_score", Double.NaN)

                if (vectorId.isNullOrEmpty() || vectorName.isNullOrEmpty() || rawData.isNullOrEmpty()) {
                    throw IllegalArgumentException("vector_id, vector_name, and raw_data are required")
                }

                val scoreText = if (!score.isNaN()) "\nCurrent sovereign score: ${score.toInt()}/100" else ""
                val prompt = """
                    Analyze identity vector $vectorId — $vectorName.
                    
                    Raw scan data:
                    $rawData
                    $scoreText
                    
                    Provide:
                    1. NUKED or KNOXED classification with confidence level
                    2. Risk severity (CRITICAL / HIGH / MEDIUM / LOW)
                    3. Top 3 immediate remediation steps
                    4. Long-term hardening recommendations
                    5. Compliance impact (GDPR / CCPA / ECRA 2026)
                """.trimIndent()

                val analysis = runBlocking(Dispatchers.IO) {
                    ArchitectAiClient.ask(prompt)
                }

                JSONObject().apply {
                    put("analysis", analysis)
                }
            }
        }
    }

    private class AuditRecommendationHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            handleRequest(exchange) {
                if (exchange.requestMethod != "POST") {
                    throw IllegalArgumentException("Only POST method is supported")
                }
                val body = parseRequestBody(exchange)
                val finding = body.optString("finding")
                val severity = body.optString("severity")
                val affectedVectors = body.optJSONArray("affected_vectors")

                if (finding.isNullOrEmpty() || severity.isNullOrEmpty() || affectedVectors == null) {
                    throw IllegalArgumentException("finding, severity, and affected_vectors are required")
                }

                val vectorsList = mutableListOf<String>()
                for (i in 0 until affectedVectors.length()) {
                    vectorsList.add(affectedVectors.getString(i))
                }

                val prompt = """
                    Generate a concise audit recommendation for the Agape Sovereign DIFF report.
                    
                    Finding: $finding
                    Severity: $severity
                    Affected vectors: ${vectorsList.joinToString(", ")}
                    
                    Format the output as:
                    ## Audit Finding
                    **Severity**: $severity
                    **Affected Vectors**: ${vectorsList.joinToString(", ")}
                    
                    ### Description
                    [2-3 sentences]
                    
                    ### Immediate Actions
                    1. ...
                    2. ...
                    3. ...
                    
                    ### Verification Steps
                    - [how to confirm it's resolved]
                    
                    ### Compliance References
                    - [relevant GDPR/CCPA/ECRA articles]
                """.trimIndent()

                val recommendation = runBlocking(Dispatchers.IO) {
                    ArchitectAiClient.ask(prompt)
                }

                JSONObject().apply {
                    put("recommendation", recommendation)
                }
            }
        }
    }

    // ── Helper functions ──────────────────────────────────────────────────────

    private fun handleRequest(exchange: HttpExchange, block: () -> JSONObject) {
        // Handle preflight OPTIONS request
        if (exchange.requestMethod == "OPTIONS") {
            sendCorsHeaders(exchange)
            exchange.sendResponseHeaders(204, -1)
            return
        }

        try {
            sendCorsHeaders(exchange)
            val responseJson = block()
            val responseBytes = responseJson.toString().toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
            exchange.sendResponseHeaders(200, responseBytes.size.toLong())
            exchange.responseBody.use { it.write(responseBytes) }
        } catch (e: IllegalArgumentException) {
            sendError(exchange, 400, e.message ?: "Bad Request")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling request", e)
            sendError(exchange, 500, "Internal Server Error: ${e.message}")
        }
    }

    private fun sendCorsHeaders(exchange: HttpExchange) {
        exchange.responseHeaders.apply {
            set("Access-Control-Allow-Origin", "*")
            set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            set("Access-Control-Allow-Headers", "Content-Type, Accept")
        }
    }

    private fun parseRequestBody(exchange: HttpExchange): JSONObject {
        val reader = BufferedReader(InputStreamReader(exchange.requestBody, Charsets.UTF_8))
        val body = reader.readText()
        return if (body.trim().isEmpty()) JSONObject() else JSONObject(body)
    }

    private fun sendError(exchange: HttpExchange, statusCode: Int, message: String) {
        try {
            sendCorsHeaders(exchange)
            val responseJson = JSONObject().apply { put("error", message) }
            val responseBytes = responseJson.toString().toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
            exchange.sendResponseHeaders(statusCode, responseBytes.size.toLong())
            exchange.responseBody.use { it.write(responseBytes) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send error response", e)
        }
    }
}
