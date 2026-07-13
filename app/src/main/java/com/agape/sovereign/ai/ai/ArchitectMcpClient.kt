package com.agape.sovereign.ai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * ArchitectMcpClient — Android REST bridge to the Architect AI MCP Server.
 *
 * The MCP server (`architect-mcp-server`) runs on the host machine and exposes
 * plain HTTP POST endpoints under `/android/*` for easy OkHttp consumption.
 *
 * Architecture:
 *   Android app  ──OkHttp──▶  architect-mcp-server (Node/Express, port 3001)
 *                                 └──▶  Ollama /api/chat (gemma4:e2b, offline)
 *
 * Default base URL:
 *   Emulator  → http://10.0.2.2:3001   (host loopback rewritten by Android)
 *   Physical  → http://<host-lan-ip>:3001
 *
 * All calls are fire-and-forget offline — no data leaves the LAN.
 */
object ArchitectMcpClient {

    const val MCP_SERVER_NAME = "Architect AI MCP"
    const val MODEL = "gemma4:e2b"

    /**
     * MCP server base URL.
     *   - Emulator: 10.0.2.2 maps to the host's 127.0.0.1
     *   - Physical device: set to the host's LAN IP, e.g. http://192.168.1.x:3001
     */
    var baseUrl: String = "http://10.0.2.2:3001"

    private val JSON = "application/json; charset=utf-8".toMediaType()

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ── Health ────────────────────────────────────────────────────────────────

    /**
     * Check if the MCP server and Ollama are reachable.
     * Returns true if the server responds with status = "ok".
     */
    suspend fun isHealthy(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/android/health")
                .get()
                .build()
            val response = http.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false
            val body = response.body?.string() ?: return@withContext false
            JSONObject(body).optString("status") == "ok"
        } catch (e: Exception) {
            false
        }
    }

    // ── ask ───────────────────────────────────────────────────────────────────

    /**
     * Ask Architect AI a general privacy / mobile-security question.
     *
     * @param question  The user's question.
     * @param context   Optional app context string (security score, scan data, etc.)
     * @return          The AI's answer, or an "ERROR: …" string on failure.
     */
    suspend fun ask(question: String, context: String? = null): String =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject().apply {
                    put("question", question)
                    if (context != null) put("context", context)
                }
                val response = post("/android/ask", body)
                response.optString("answer", "ERROR: Unexpected response from MCP server")
            } catch (e: Exception) {
                "ERROR: MCP server unreachable (${e.message}). Is `npm run dev` running?"
            }
        }

    // ── analyze_vector ────────────────────────────────────────────────────────

    /**
     * Analyze one of the 16 sovereign identity vectors.
     *
     * @param vectorId       e.g. "V-01" or "email_breach"
     * @param vectorName     Human-readable name
     * @param rawData        Scan data / description of the exposure
     * @param sovereignScore Current overall score (0-100), optional
     * @return               NUKED/KNOXED analysis with remediation steps.
     */
    suspend fun analyzeVector(
        vectorId: String,
        vectorName: String,
        rawData: String,
        sovereignScore: Int? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("vector_id", vectorId)
                put("vector_name", vectorName)
                put("raw_data", rawData)
                if (sovereignScore != null) put("sovereign_score", sovereignScore)
            }
            val response = post("/android/analyze_vector", body)
            response.optString("analysis", "ERROR: Unexpected response from MCP server")
        } catch (e: Exception) {
            "ERROR: MCP server unreachable (${e.message})"
        }
    }

    // ── audit_recommendation ──────────────────────────────────────────────────

    /**
     * Generate a sovereign audit recommendation for a security finding.
     *
     * @param finding          Description of the finding.
     * @param severity         "CRITICAL" | "HIGH" | "MEDIUM" | "LOW"
     * @param affectedVectors  List of affected vector IDs.
     * @return                 Formatted audit recommendation section.
     */
    suspend fun auditRecommendation(
        finding: String,
        severity: String,
        affectedVectors: List<String>
    ): String = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("finding", finding)
                put("severity", severity)
                put("affected_vectors", JSONArray(affectedVectors))
            }
            val response = post("/android/audit_recommendation", body)
            response.optString("recommendation", "ERROR: Unexpected response from MCP server")
        } catch (e: Exception) {
            "ERROR: MCP server unreachable (${e.message})"
        }
    }

    // ── internal helper ───────────────────────────────────────────────────────

    private fun post(path: String, bodyJson: JSONObject): JSONObject {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .post(bodyJson.toString().toRequestBody(JSON))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("HTTP ${response.code}: ${response.message}")
        }
        val raw = response.body?.string()
            ?: throw RuntimeException("Empty response body")
        return JSONObject(raw)
    }
}
