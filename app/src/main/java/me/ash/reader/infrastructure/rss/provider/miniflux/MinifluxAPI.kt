package me.ash.reader.infrastructure.rss.provider.miniflux

import android.content.Context
import me.ash.reader.infrastructure.exception.MinifluxAPIException
import me.ash.reader.infrastructure.net.RetryConfig
import me.ash.reader.infrastructure.net.withRetries
import me.ash.reader.infrastructure.rss.provider.ProviderAPI
import me.ash.reader.ui.ext.encodeBase64
import me.ash.reader.ui.ext.md5
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.executeAsync
import java.util.concurrent.ConcurrentHashMap

class MinifluxAPI private constructor(
    context: Context,
    private val serverUrl: String,
    private val apiToken: String? = null,
    private val username: String? = null,
    private val password: String? = null,
    private val httpUsername: String? = null,
    private val httpPassword: String? = null,
    clientCertificateAlias: String? = null,
) : ProviderAPI(context, clientCertificateAlias) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val retryConfig = RetryConfig()

    private fun normalizeUrl(url: String): String {
        return url.trimEnd('/')
    }

    private suspend inline fun <reified T> executeRequest(
        method: String,
        endpoint: String,
        bodyObj: Any? = null,
        queryParams: Map<String, String?>? = null,
    ): T {
        val baseUrl = normalizeUrl(serverUrl)
        val fullPath = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        val httpUrlBuilder = ("$baseUrl$fullPath").toHttpUrlOrNull()?.newBuilder()
            ?: throw MinifluxAPIException("Invalid server URL: $serverUrl", endpoint = endpoint)

        queryParams?.forEach { (key, value) ->
            if (value != null) {
                httpUrlBuilder.addQueryParameter(key, value)
            }
        }

        val requestUrl = httpUrlBuilder.build()
        val requestBuilder = Request.Builder().url(requestUrl)

        // Authentication headers
        when {
            !apiToken.isNullOrBlank() -> {
                requestBuilder.addHeader("X-Auth-Token", apiToken)
            }
            !username.isNullOrBlank() -> {
                val credential = "$username:$password".encodeBase64()
                requestBuilder.addHeader("Authorization", "Basic $credential")
            }
        }

        // Reverse proxy HTTP Basic Auth if present
        if (!httpUsername.isNullOrBlank()) {
            val httpCred = "$httpUsername:$httpPassword".encodeBase64()
            requestBuilder.addHeader("Authorization", "Basic $httpCred")
        }

        // Body
        val requestBody = when {
            bodyObj != null -> gson.toJson(bodyObj).toRequestBody(jsonMediaType)
            method == "POST" || method == "PUT" -> "".toRequestBody(jsonMediaType)
            else -> null
        }

        requestBuilder.method(method, requestBody)

        val response = try {
            client.newCall(requestBuilder.build()).executeAsync()
        } catch (e: Exception) {
            throw MinifluxAPIException("Network request failed: ${e.message}", endpoint = endpoint, cause = e)
        }

        val code = response.code
        val responseBodyStr = response.body.string()

        if (code == 401) {
            throw MinifluxAPIException("Unauthorized", statusCode = 401, endpoint = endpoint)
        }
        if (code == 403) {
            throw MinifluxAPIException("Forbidden", statusCode = 403, endpoint = endpoint)
        }
        if (code !in 200..299) {
            val errorMsg = try {
                gson.fromJson(responseBodyStr, MinifluxDTO.ErrorResponse::class.java)?.errorMessage
            } catch (_: Exception) {
                null
            } ?: "HTTP error $code"
            throw MinifluxAPIException(errorMsg, statusCode = code, endpoint = endpoint)
        }

        if (Unit is T || responseBodyStr.isEmpty()) {
            @Suppress("UNCHECKED_CAST")
            return Unit as T
        }

        return try {
            toDTO<T>(responseBodyStr)
        } catch (e: Exception) {
            throw MinifluxAPIException("Unable to parse response for $endpoint", statusCode = code, endpoint = endpoint, cause = e)
        }
    }

    // --- API Endpoints ---

    /** GET /v1/me */
    suspend fun getMe(): MinifluxDTO.User =
        executeRequest("GET", "/v1/me")

    /** Test credentials by fetching /v1/me */
    suspend fun validCredentials(): Boolean = try {
        getMe().id > 0
    } catch (_: Exception) {
        false
    }

    /** GET /v1/categories?counts=true */
    suspend fun getCategories(includeCounts: Boolean = true): List<MinifluxDTO.Category> =
        executeRequest("GET", "/v1/categories", queryParams = mapOf("counts" to includeCounts.toString()))

    /** POST /v1/categories */
    suspend fun createCategory(title: String, hideGlobally: Boolean? = null): MinifluxDTO.Category =
        executeRequest("POST", "/v1/categories", bodyObj = MinifluxDTO.CreateCategoryRequest(title, hideGlobally))

    /** PUT /v1/categories/{id} */
    suspend fun updateCategory(categoryId: Long, title: String? = null, hideGlobally: Boolean? = null): MinifluxDTO.Category =
        executeRequest("PUT", "/v1/categories/$categoryId", bodyObj = MinifluxDTO.UpdateCategoryRequest(title, hideGlobally))

    /** DELETE /v1/categories/{id} */
    suspend fun deleteCategory(categoryId: Long) {
        executeRequest<Unit>("DELETE", "/v1/categories/$categoryId")
    }

    /** PUT /v1/categories/{id}/mark-all-as-read */
    suspend fun markCategoryAsRead(categoryId: Long) {
        withRetries(retryConfig) {
            executeRequest<Unit>("PUT", "/v1/categories/$categoryId/mark-all-as-read")
        }.getOrThrow()
    }

    /** GET /v1/feeds */
    suspend fun getFeeds(): List<MinifluxDTO.Feed> =
        executeRequest("GET", "/v1/feeds")

    /** GET /v1/categories/{id}/feeds */
    suspend fun getCategoryFeeds(categoryId: Long): List<MinifluxDTO.Feed> =
        executeRequest("GET", "/v1/categories/$categoryId/feeds")

    /** GET /v1/feeds/{id} */
    suspend fun getFeed(feedId: Long): MinifluxDTO.Feed =
        executeRequest("GET", "/v1/feeds/$feedId")

    /** POST /v1/feeds */
    suspend fun createFeed(
        feedUrl: String,
        categoryId: Long? = null,
        crawler: Boolean? = null,
    ): MinifluxDTO.CreateFeedResponse =
        executeRequest("POST", "/v1/feeds", bodyObj = MinifluxDTO.CreateFeedRequest(feedUrl = feedUrl, categoryId = categoryId, crawler = crawler))

    /** PUT /v1/feeds/{id} */
    suspend fun updateFeed(
        feedId: Long,
        title: String? = null,
        categoryId: Long? = null,
    ): MinifluxDTO.Feed =
        executeRequest("PUT", "/v1/feeds/$feedId", bodyObj = MinifluxDTO.UpdateFeedRequest(title = title, categoryId = categoryId))

    /** DELETE /v1/feeds/{id} */
    suspend fun removeFeed(feedId: Long) {
        executeRequest<Unit>("DELETE", "/v1/feeds/$feedId")
    }

    /** PUT /v1/feeds/{id}/refresh */
    suspend fun refreshFeed(feedId: Long) {
        executeRequest<Unit>("PUT", "/v1/feeds/$feedId/refresh")
    }

    /** PUT /v1/feeds/refresh */
    suspend fun refreshAllFeeds() {
        executeRequest<Unit>("PUT", "/v1/feeds/refresh")
    }

    /** GET /v1/feeds/{id}/icon */
    suspend fun getFeedIcon(feedId: Long): MinifluxDTO.Icon =
        executeRequest("GET", "/v1/feeds/$feedId/icon")

    /** GET /v1/entries */
    suspend fun getEntries(
        status: String? = null,
        starred: Boolean? = null,
        after: Long? = null,
        before: Long? = null,
        changedAfter: Long? = null,
        changedBefore: Long? = null,
        publishedAfter: Long? = null,
        publishedBefore: Long? = null,
        limit: Int? = null,
        offset: Int? = null,
        feedId: Long? = null,
        categoryId: Long? = null,
        order: String? = "published_at",
        direction: String? = "desc",
    ): MinifluxDTO.EntriesResponse =
        executeRequest(
            "GET",
            "/v1/entries",
            queryParams = mapOf(
                "status" to status,
                "starred" to starred?.toString(),
                "after" to after?.toString(),
                "before" to before?.toString(),
                "changed_after" to changedAfter?.toString(),
                "changed_before" to changedBefore?.toString(),
                "published_after" to publishedAfter?.toString(),
                "published_before" to publishedBefore?.toString(),
                "limit" to limit?.toString(),
                "offset" to offset?.toString(),
                "feed_id" to feedId?.toString(),
                "category_id" to categoryId?.toString(),
                "order" to order,
                "direction" to direction,
            )
        )

    /** GET /v1/entries/{id} */
    suspend fun getEntry(entryId: Long): MinifluxDTO.Entry =
        executeRequest("GET", "/v1/entries/$entryId")

    /** GET /v1/entries/ids */
    suspend fun getEntryIds(
        status: String? = null,
        starred: Boolean? = null,
    ): MinifluxDTO.EntryIDsResponse =
        executeRequest(
            "GET",
            "/v1/entries/ids",
            queryParams = mapOf(
                "status" to status,
                "starred" to starred?.toString(),
            )
        )

    /** PUT /v1/entries */
    suspend fun updateEntries(
        entryIds: List<Long>,
        status: String? = null,
        starred: Boolean? = null,
    ) {
        if (entryIds.isEmpty()) return
        withRetries(retryConfig) {
            executeRequest<Unit>(
                "PUT",
                "/v1/entries",
                bodyObj = MinifluxDTO.UpdateEntriesRequest(entryIds = entryIds, status = status, starred = starred)
            )
        }.getOrThrow()
    }

    /** PUT /v1/entries/{id}/bookmark */
    suspend fun toggleBookmark(entryId: Long) {
        withRetries(retryConfig) {
            executeRequest<Unit>("PUT", "/v1/entries/$entryId/bookmark")
        }.getOrThrow()
    }

    /** POST /v1/discover */
    suspend fun discover(url: String): List<MinifluxDTO.DiscoverSubscription> =
        executeRequest("POST", "/v1/discover", bodyObj = MinifluxDTO.DiscoverRequest(url))

    /** GET /v1/entries/{id}/fetch-content */
    suspend fun fetchOriginalContent(entryId: Long): Map<String, String> =
        executeRequest("GET", "/v1/entries/$entryId/fetch-content")

    companion object {
        private val instances: ConcurrentHashMap<String, MinifluxAPI> = ConcurrentHashMap()

        fun getInstance(
            context: Context,
            serverUrl: String,
            apiToken: String? = null,
            username: String? = null,
            password: String? = null,
            httpUsername: String? = null,
            httpPassword: String? = null,
            clientCertificateAlias: String? = null,
        ): MinifluxAPI {
            val key = "${serverUrl.trimEnd('/')}|$apiToken|$username|$password|$httpUsername|$httpPassword|$clientCertificateAlias".md5()
            return instances.getOrPut(key) {
                MinifluxAPI(
                    context = context,
                    serverUrl = serverUrl,
                    apiToken = apiToken,
                    username = username,
                    password = password,
                    httpUsername = httpUsername,
                    httpPassword = httpPassword,
                    clientCertificateAlias = clientCertificateAlias,
                )
            }
        }

        fun clearInstance() {
            instances.clear()
        }
    }
}
