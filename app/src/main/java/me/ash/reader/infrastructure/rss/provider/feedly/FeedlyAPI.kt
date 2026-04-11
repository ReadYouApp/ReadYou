package me.ash.reader.infrastructure.rss.provider.feedly

import android.content.Context
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import me.ash.reader.infrastructure.exception.FeedlyAPIException
import me.ash.reader.infrastructure.net.RetryConfig
import me.ash.reader.infrastructure.net.withRetries
import me.ash.reader.infrastructure.rss.provider.ProviderAPI
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.executeAsync

class FeedlyAPI
private constructor(
    context: Context,
    private val accessToken: String,
) : ProviderAPI(context, null) {

    private val baseUrl = "https://cloud.feedly.com/v3/"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val retryConfig = RetryConfig()

    private suspend inline fun <reified T> getRequest(
        path: String,
        params: List<Pair<String, String>>? = null,
    ): T {
        val urlBuilder = StringBuilder("$baseUrl$path")
        if (!params.isNullOrEmpty()) {
            urlBuilder.append("?")
            urlBuilder.append(params.joinToString("&") { "${it.first}=${it.second}" })
        }

        val response =
            client
                .newCall(
                    Request.Builder()
                        .url(urlBuilder.toString())
                        .addHeader("Authorization", "OAuth $accessToken")
                        .get()
                        .build()
                )
                .executeAsync()

        val body = response.body.string()
        when (response.code) {
            401 -> throw FeedlyAPIException("Unauthorized: Invalid access token")
            403 -> throw FeedlyAPIException("Forbidden")
            !in 200..299 -> throw FeedlyAPIException("Error ${response.code}: $body")
        }
        return toDTO(body)
    }

    private suspend inline fun <reified T> postRequest(path: String, bodyObj: Any): T {
        val jsonBody = gson.toJson(bodyObj).toRequestBody(jsonMediaType)

        val response =
            client
                .newCall(
                    Request.Builder()
                        .url("$baseUrl$path")
                        .addHeader("Authorization", "OAuth $accessToken")
                        .post(jsonBody)
                        .build()
                )
                .executeAsync()

        val responseBody = response.body.string()
        when (response.code) {
            401 -> throw FeedlyAPIException("Unauthorized: Invalid access token")
            403 -> throw FeedlyAPIException("Forbidden")
            !in 200..299 -> throw FeedlyAPIException("Error ${response.code}: $responseBody")
        }
        @Suppress("UNCHECKED_CAST")
        return if (responseBody.isBlank()) "" as T else toDTO(responseBody)
    }

    private suspend fun deleteRequest(path: String) {
        val response =
            client
                .newCall(
                    Request.Builder()
                        .url("$baseUrl$path")
                        .addHeader("Authorization", "OAuth $accessToken")
                        .delete()
                        .build()
                )
                .executeAsync()

        val responseBody = response.body.string()
        when (response.code) {
            401 -> throw FeedlyAPIException("Unauthorized: Invalid access token")
            403 -> throw FeedlyAPIException("Forbidden")
            !in 200..299 -> throw FeedlyAPIException("Error ${response.code}: $responseBody")
        }
    }

    suspend fun getProfile(): FeedlyDTO.Profile = getRequest("profile")

    suspend fun getSubscriptions(): List<FeedlyDTO.Subscription> =
        getRequest<Array<FeedlyDTO.Subscription>>("subscriptions").toList()

    suspend fun getCollections(): List<FeedlyDTO.Collection> =
        getRequest<Array<FeedlyDTO.Collection>>("collections").toList()

    suspend fun getStreamContents(
        streamId: String,
        count: Int = 250,
        continuation: String? = null,
        newerThan: Long? = null,
    ): FeedlyDTO.StreamContents {
        val encodedStreamId = URLEncoder.encode(streamId, "UTF-8")
        val params =
            mutableListOf("count" to count.toString()).apply {
                continuation?.let { add("continuation" to it) }
                newerThan?.let { add("newerThan" to it.toString()) }
            }
        return getRequest("streams/$encodedStreamId/contents", params)
    }

    suspend fun markEntriesAsRead(entryIds: List<String>) {
        if (entryIds.isEmpty()) return
        withRetries(retryConfig) {
            postRequest<String>(
                "markers",
                FeedlyDTO.MarkersRequest(
                    action = "markAsRead",
                    type = "entries",
                    entryIds = entryIds,
                ),
            )
        }.getOrThrow()
    }

    suspend fun markEntriesAsUnread(entryIds: List<String>) {
        if (entryIds.isEmpty()) return
        withRetries(retryConfig) {
            postRequest<String>(
                "markers",
                FeedlyDTO.MarkersRequest(
                    action = "keepUnread",
                    type = "entries",
                    entryIds = entryIds,
                ),
            )
        }.getOrThrow()
    }

    suspend fun markFeedAsRead(feedId: String, asOf: Long? = null) {
        withRetries(retryConfig) {
            postRequest<String>(
                "markers",
                FeedlyDTO.MarkersRequest(
                    action = "markAsRead",
                    type = "feeds",
                    feedIds = listOf(feedId),
                    asOf = asOf,
                ),
            )
        }.getOrThrow()
    }

    suspend fun markCategoryAsRead(categoryId: String, asOf: Long? = null) {
        withRetries(retryConfig) {
            postRequest<String>(
                "markers",
                FeedlyDTO.MarkersRequest(
                    action = "markAsRead",
                    type = "categories",
                    categoryIds = listOf(categoryId),
                    asOf = asOf,
                ),
            )
        }.getOrThrow()
    }

    suspend fun markEntriesAsSaved(entryIds: List<String>) {
        if (entryIds.isEmpty()) return
        withRetries(retryConfig) {
            postRequest<String>(
                "markers",
                FeedlyDTO.MarkersRequest(
                    action = "markAsSaved",
                    type = "entries",
                    entryIds = entryIds,
                ),
            )
        }.getOrThrow()
    }

    suspend fun markEntriesAsUnsaved(entryIds: List<String>) {
        if (entryIds.isEmpty()) return
        withRetries(retryConfig) {
            postRequest<String>(
                "markers",
                FeedlyDTO.MarkersRequest(
                    action = "markAsUnsaved",
                    type = "entries",
                    entryIds = entryIds,
                ),
            )
        }.getOrThrow()
    }

    suspend fun addSubscription(
        feedId: String,
        title: String?,
        categoryId: String?,
    ): FeedlyDTO.Subscription {
        val body =
            mutableMapOf<String, Any>("id" to feedId).apply {
                title?.let { put("title", it) }
                categoryId?.let {
                    put(
                        "categories",
                        listOf(mapOf("id" to it)),
                    )
                }
            }
        return postRequest("subscriptions", body)
    }

    suspend fun deleteSubscription(feedId: String) {
        val encodedFeedId = URLEncoder.encode(feedId, "UTF-8")
        deleteRequest("subscriptions/$encodedFeedId")
    }

    companion object {

        private val instances: ConcurrentHashMap<String, FeedlyAPI> = ConcurrentHashMap()

        fun getInstance(context: Context, accessToken: String): FeedlyAPI =
            instances.getOrPut(accessToken) { FeedlyAPI(context, accessToken) }

        fun clearInstance() {
            instances.clear()
        }
    }
}
