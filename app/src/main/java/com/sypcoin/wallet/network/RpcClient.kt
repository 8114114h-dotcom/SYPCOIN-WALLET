package com.sypcoin.wallet.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class RpcClient(private var rpcUrl: String) {

    private val idCounter = AtomicLong(1)

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30,    TimeUnit.SECONDS)
        .writeTimeout(10,   TimeUnit.SECONDS)
        .build()

    fun setUrl(url: String) { rpcUrl = url }
    fun getUrl(): String    = rpcUrl

    suspend fun getBalance(address: String): String {
        val result = call("getBalance", listOf(address))
        return result.getString("result")
    }

    suspend fun getNonce(address: String): Long {
        val result = call("getNonce", listOf(address))
        return result.getLong("result")
    }

    suspend fun sendTransaction(txHex: String): String {
        val result = call("sendTransaction", listOf(txHex))
        return result.getString("result")
    }

    suspend fun getTransaction(txId: String): TxInfo {
        val result = call("getTransaction", listOf(txId))
        val obj    = result.getJSONObject("result")
        return TxInfo(
            txId        = obj.getString("tx_id"),
            from        = obj.optString("from", ""),
            to          = obj.getString("to"),
            amount      = obj.getString("amount"),
            fee         = obj.optString("fee", "0.000001"),
            nonce       = obj.getLong("nonce"),
            blockHeight = if (obj.isNull("block_height")) null else obj.getLong("block_height"),
            blockHash   = if (obj.isNull("block_hash"))   null else obj.getString("block_hash"),
            timestamp   = obj.getLong("timestamp")
        )
    }

    suspend fun getBlockHeight(): Long {
        val result = call("getBlockHeight", emptyList<Any>())
        return result.getLong("result")
    }

    suspend fun getMiningInfo(): MiningInfo {
        val result = call("getMiningInfo", emptyList<Any>())
        val obj    = result.getJSONObject("result")
        return MiningInfo(
            height     = obj.getLong("height"),
            difficulty = obj.getLong("difficulty"),
            bestHash   = obj.getString("best_hash"),
            target     = obj.getString("target")
        )
    }

    private suspend fun call(method: String, params: List<Any>): JSONObject =
        withContext(Dispatchers.IO) {
            val id   = idCounter.getAndIncrement()
            val body = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("method",  method)
                put("params",  org.json.JSONArray(params))
                put("id",      id)
            }.toString()

            val request = Request.Builder()
                .url(rpcUrl)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            val response     = http.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: throw RpcException("Empty response from node")

            val json = JSONObject(responseBody)

            if (json.has("error") && !json.isNull("error")) {
                val err = json.getJSONObject("error")
                throw RpcException("${err.optString("message", "RPC error")} (code ${err.optInt("code")})")
            }

            json
        }
}

data class TxInfo(
    val txId:        String,
    val from:        String,
    val to:          String,
    val amount:      String,
    val fee:         String,
    val nonce:       Long,
    val blockHeight: Long?,
    val blockHash:   String?,
    val timestamp:   Long
)

data class MiningInfo(
    val height:     Long,
    val difficulty: Long,
    val bestHash:   String,
    val target:     String
)

class RpcException(message: String) : Exception(message)
