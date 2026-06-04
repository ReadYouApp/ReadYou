package me.ash.reader.infrastructure.rss.provider.greader

import android.content.Context
import androidx.annotation.CheckResult
import java.util.concurrent.ConcurrentHashMap
import kotlin.text.hexToLong
import kotlin.text.startsWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.ash.reader.domain.data.SyncLogger
import me.ash.reader.infrastructure.di.USER_AGENT_STRING
import me.ash.reader.infrastructure.exception.GoogleReaderAPIException
import me.ash.reader.infrastructure.exception.RetryException
import me.ash.reader.infrastructure.net.ApiResult
import me.ash.reader.infrastructure.net.RetryConfig
import me.ash.reader.infrastructure.net.RetryableTaskResult
import me.ash.reader.infrastructure.net.getOrThrow
import me.ash.reader.infrastructure.net.onFailure
import me.ash.reader.infrastructure.net.onSuccess
import me.ash.reader.infrastructure.net.withRetries
import me.ash.reader.infrastructure.rss.provider.ProviderAPI
import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.spacerDollar
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.executeAsync
import okio.IOException
import timber.log.Timber

private const val TAG = "GoogleReaderAPI"

class GoogleReaderAPI
private constructor(
    context: Context,
    private val serverUrl: String,
    private val username: String,
    private val password: String,
    private val httpUsername: String? = null,
    private val httpPassword: String? = null,
    private val syncLogger: SyncLogger,
    clientCertificateAlias: String? = null,
) : ProviderAPI(context, clientCertificateAlias) {

    sealed class Stream(val tag: String) {
        data object AllItems : Stream("user/-/state/com.google/reading-list")

        data object Read : Stream("user/-/state/com.google/read")

        data object Starred : Stream("user/-/state/com.google/starred")

        data object Like : Stream("user/-/state/com.google/like")

        data object Broadcast : Stream("user/-/state/com.google/broadcast")

        data class Feed(val feedId: String) : Stream("feed/$feedId")

        data class Category(val categoryId: String) : Stream("user/-/label/$categoryId")
    }

    private data class AuthData(val clientLoginToken: String?, val actionToken: String?)

    private val authDataStateFlow = MutableStateFlow(AuthData(null, null))

    fun clearAuthData() = authDataStateFlow.update { it.copy(null, null) }

    private var authData
        get() = authDataStateFlow.value
        set(value) {
            authDataStateFlow.value = value
        }

    suspend fun validCredentials(): Boolean {
        val result = reAuthenticate().onSuccess { authData = it }
        return result.isSuccess
    }

    suspend fun refreshCredentialsIfNeeded() {
        if (authData.clientLoginToken.isNullOrEmpty()) {
            reAuthenticate().getOrThrow().let { authData = it }
        }
    }

    @CheckResult
    private suspend fun reAuthenticate(): ApiResult<AuthData> {
        // Get client login token
        val clResponse =
            try {
                client
                    .newCall(
                        Request.Builder()
                            .url("${serverUrl}accounts/ClientLogin")
                            .header("User-Agent", USER_AGENT_STRING)
                            .post(
                                FormBody.Builder()
                                    .add("output", "json")
                                    .add("Email", username)
                                    .add("Passwd", password)
                                    .add("client", "ReadYou")
                                    .add("accountType", "HOSTED_OR_GOOGLE")
                                    .add("service", "reader")
                                    .build()
                            )
                            .build()
                    )
                    .executeAsync()
            } catch (e: IOException) {
                return ApiResult.NetworkError(e)
            }

        val clBody = clResponse.body.string()
        when (clResponse.code) {
            400 -> return ApiResult.BizError(GoogleReaderAPIException("BadRequest for CL Token"))
            401 -> return ApiResult.BizError(GoogleReaderAPIException("Unauthorized for CL Token"))
            !in 200..299 -> {
                return ApiResult.BizError(GoogleReaderAPIException(clBody))
            }
        }

        val loginToken =
            try {
                toDTO<GoogleReaderDTO.MinifluxAuthData>(clBody).Auth
            } catch (ignore: Exception) {
                clBody.split("\n").find { it.startsWith("Auth=") }?.substring(5)
                    ?: return ApiResult.BizError(
                        GoogleReaderAPIException("body format error for CL Token:\n$clBody")
                    )
            }

        // Get action token
        val actResponse =
            try {
                client
                    .newCall(
                        Request.Builder()
                            .url("${serverUrl}reader/api/0/token")
                            .header("Authorization", "GoogleLogin auth=${loginToken}")
                            .get()
                            .build()
                    )
                    .executeAsync()
            } catch (e: IOException) {
                return ApiResult.NetworkError(e)
            }
        val actionToken = actResponse.body.string()

        if (actResponse.code !in 200..299) {
            // It's not used currently but may be used later the same way Google Reader uses it
            // (expires in 30 minutes, with "x-reader-google-bad-token: true" header set).
        }
        return ApiResult.Success(AuthData(actionToken = actionToken, clientLoginToken = loginToken))
    }

    private val retryConfig =
        RetryConfig(
            onRetry = {
                Timber.e(it)
                clearAuthData()
            }
        )

    private suspend inline fun <reified T> retryableGetRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
    ): T {
        return withRetries(retryConfig) { getRequest<T>(query, params) }.getOrThrow()
    }

    private suspend inline fun <reified T> retryablePostRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
        form: List<Pair<String, String>>? = null,
    ): T {
        return withRetries(retryConfig) { postRequest<T>(query, params, form) }
            .onFailure {
                if (it is GoogleReaderAPIException) {
                    syncLogger.log(it.copy(query = query, params = params, form = form))
                } else {
                    syncLogger.log(
                        GoogleReaderAPIException(
                            message = it.message.toString(),
                            query = query,
                            params = params,
                            form = form,
                        )
                    )
                }
            }
            .getOrThrow()
    }

    @CheckResult
    private suspend inline fun <reified T> retryableGetRequestWithResult(
        query: String,
        params: List<Pair<String, String>>? = null,
    ): RetryableTaskResult<T> {
        return withRetries(retryConfig) { getRequest<T>(query, params) }
    }

    @CheckResult
    private suspend inline fun <reified T> retryablePostRequestWithResult(
        query: String,
        params: List<Pair<String, String>>? = null,
        form: List<Pair<String, String>>? = null,
    ): RetryableTaskResult<T> {
        return withRetries(retryConfig) { postRequest<T>(query, params, form) }
    }

    private suspend inline fun <reified T> getRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
    ): T {
        refreshCredentialsIfNeeded()

        val response =
            client
                .newCall(
                    Request.Builder()
                        .url(
                            "$serverUrl$query?output=json${params?.joinToString(separator = "") { "&${it.first}=${it.second}" } ?: ""}"
                        )
                        .addHeader("Authorization", "GoogleLogin auth=${authData.clientLoginToken}")
                        .addHeader("User-Agent", USER_AGENT_STRING)
                        .get()
                        .build()
                )
                .executeAsync()

        val body = response.body.string()
        when (response.code) {
            400 -> throw GoogleReaderAPIException("BadRequest")
            401 -> throw RetryException("Unauthorized")
            !in 200..299 -> {
                val gReaderError =
                    try {
                        toDTO<GoogleReaderDTO.GReaderError>(body)
                    } catch (ignore: Exception) {
                        GoogleReaderDTO.GReaderError(listOf(body))
                    }
                throw GoogleReaderAPIException(gReaderError.errors.joinToString(";\n "))
            }
        }

        return toDTO(body)
    }

    private suspend inline fun <reified T> postRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
        form: List<Pair<String, String>>? = null,
    ): T {
        refreshCredentialsIfNeeded()
        val response =
            client
                .newCall(
                    Request.Builder()
                        .url(
                            "$serverUrl$query?output=json${params?.joinToString(separator = "") { "&${it.first}=${it.second}" } ?: ""}"
                        )
                        .addHeader("Authorization", "GoogleLogin auth=${authData.clientLoginToken}")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("User-Agent", USER_AGENT_STRING)
                        .post(
                            FormBody.Builder()
                                .apply {
                                    form?.forEach { add(it.first, it.second) }
                                    authData.actionToken?.let { add("T", it) }
                                }
                                .build()
                        )
                        .build()
                )
                .executeAsync()

        val responseBody = response.body.string()
        when (response.code) {
            400 -> throw GoogleReaderAPIException("BadRequest")
            401 -> throw RetryException("Unauthorized")
            !in 200..299 -> {
                throw GoogleReaderAPIException(responseBody)
            }
        }

        return toDTO(responseBody)
    }

    suspend fun getUserInfo(): GoogleReaderDTO.User =
        retryableGetRequest<GoogleReaderDTO.User>("reader/api/0/user-info")

    suspend fun getSubscriptionList(): GoogleReaderDTO.SubscriptionList? =
        retryableGetRequestWithResult<GoogleReaderDTO.SubscriptionList>(
                "reader/api/0/subscription/list"
            )
            .getOrNull()

    suspend fun getReadItemIds(
        since: Long,
        useIt: Boolean = false,
        limit: String? = MAXIMUM_ITEMS_LIMIT,
        continuationId: String? = null,
    ): GoogleReaderDTO.ItemIds? =
        retryableGetRequestWithResult<GoogleReaderDTO.ItemIds>(
                query = "reader/api/0/stream/items/ids",
                params =
                    mutableListOf<Pair<String, String>>().apply {
                        if (useIt) add("s" to Stream.AllItems.tag)
                        add(Pair(if (useIt) "it" else "s", Stream.Read.tag))
                        add(Pair("ot", since.toString()))
                        limit?.let { add(Pair("n", limit)) }
                        continuationId?.let { add(Pair("c", continuationId)) }
                    },
            )
            .getOrNull()

    suspend fun getItemIdsForFeed(
        feedId: String,
        filterRead: Boolean = false,
        since: Long? = null,
        limit: String? = MAXIMUM_ITEMS_LIMIT,
        continuationId: String? = null,
    ): GoogleReaderDTO.ItemIds? =
        retryableGetRequestWithResult<GoogleReaderDTO.ItemIds>(
                query = "reader/api/0/stream/items/ids",
                params =
                    mutableListOf<Pair<String, String>>().apply {
                        add(Pair("s", Stream.Feed(feedId).tag))
                        if (filterRead) add(Pair("xt", Stream.Read.tag))
                        limit?.let { add(Pair("n", limit)) }
                        since?.let { add(Pair("ot", since.toString())) }
                        continuationId?.let { add(Pair("c", continuationId)) }
                    },
            )
            .getOrNull()

    suspend fun getItemIdsForCategory(
        categoryId: String,
        filterRead: Boolean = false,
        since: Long? = null,
        limit: String? = MAXIMUM_ITEMS_LIMIT,
        continuationId: String? = null,
    ): GoogleReaderDTO.ItemIds? =
        retryableGetRequestWithResult<GoogleReaderDTO.ItemIds>(
                query = "reader/api/0/stream/items/ids",
                params =
                    mutableListOf<Pair<String, String>>().apply {
                        add(Pair("s", Stream.Category(categoryId).tag))
                        if (filterRead) add(Pair("xt", Stream.Read.tag))
                        limit?.let { add(Pair("n", limit)) }
                        since?.let { add(Pair("ot", since.toString())) }
                        continuationId?.let { add(Pair("c", continuationId)) }
                    },
            )
            .getOrNull()

    suspend fun getUnreadItemIds(
        since: Long? = null,
        limit: String? = MAXIMUM_ITEMS_LIMIT,
        continuationId: String? = null,
    ): GoogleReaderDTO.ItemIds? =
        retryableGetRequestWithResult<GoogleReaderDTO.ItemIds>(
                query = "reader/api/0/stream/items/ids",
                params =
                    mutableListOf<Pair<String, String>>().apply {
                        add(Pair("s", Stream.AllItems.tag))
                        add(Pair("xt", Stream.Read.tag))
                        limit?.let { add(Pair("n", limit)) }
                        since?.let { add(Pair("ot", since.toString())) }
                        continuationId?.let { add(Pair("c", continuationId)) }
                    },
            )
            .getOrNull()

    suspend fun getStarredItemIds(
        since: Long? = null,
        limit: String? = MAXIMUM_ITEMS_LIMIT,
        continuationId: String? = null,
    ): GoogleReaderDTO.ItemIds? =
        retryableGetRequestWithResult<GoogleReaderDTO.ItemIds>(
                query = "reader/api/0/stream/items/ids",
                params =
                    mutableListOf<Pair<String, String>>().apply {
                        add(Pair("s", Stream.Starred.tag))
                        limit?.let { add(Pair("n", limit)) }
                        since?.let { add(Pair("ot", since.toString())) }
                        continuationId?.let { add(Pair("c", continuationId)) }
                    },
            )
            .getOrNull()

    suspend fun getItemsContents(ids: List<String>?) =
        retryablePostRequest<GoogleReaderDTO.ItemsContents>(
            query = "reader/api/0/stream/items/contents",
            form = ids?.map { Pair("i", it) },
        )

    suspend fun subscriptionQuickAdd(feedUrl: String): GoogleReaderDTO.QuickAddFeed =
        retryablePostRequest<GoogleReaderDTO.QuickAddFeed>(
            query = "reader/api/0/subscription/quickadd",
            params = listOf(Pair("quickadd", feedUrl)),
            form = listOf(Pair("quickadd", feedUrl)),
        )

    @CheckResult
    suspend fun editTag(
        itemIds: List<String>,
        mark: String? = null,
        unmark: String? = null,
    ): RetryableTaskResult<String> =
        retryablePostRequestWithResult<String>(
            query = "reader/api/0/edit-tag",
            form =
                mutableListOf<Pair<String, String>>().apply {
                    itemIds.forEach { add(Pair("i", it)) }
                    mark?.let { add(Pair("a", mark)) }
                    unmark?.let { add(Pair("r", unmark)) }
                },
        )

    suspend fun disableTag(categoryId: String): String =
        retryablePostRequest<String>(
            query = "reader/api/0/disable-tag",
            form = listOf(Pair("s", categoryId.ofCategoryIdToStreamId())),
        )

    suspend fun renameTag(categoryId: String, renameToName: String): String =
        retryablePostRequest<String>(
            query = "reader/api/0/rename-tag",
            form =
                listOf(
                    Pair("s", categoryId.ofCategoryIdToStreamId()),
                    Pair("dest", renameToName.ofCategoryIdToStreamId()),
                ),
        )

    suspend fun subscriptionEdit(
        action: String = "edit",
        destFeedId: String? = null,
        destCategoryId: String? = null,
        originCategoryId: String? = null,
        destFeedName: String? = null,
    ): String =
        retryablePostRequest<String>(
            query = "reader/api/0/subscription/edit",
            form =
                mutableListOf(Pair("ac", action)).apply {
                    destFeedId?.let { add(Pair("s", it.ofFeedIdToStreamId())) }
                    destCategoryId?.let { add(Pair("a", it.ofCategoryIdToStreamId())) }
                    originCategoryId?.let { add(Pair("r", it.ofCategoryIdToStreamId())) }
                    destFeedName?.takeIf { it.isNotBlank() }?.let { add(Pair("t", destFeedName)) }
                },
        )

    // Not all services support it
    suspend fun markAllAsRead(streamId: String, sinceTimestamp: Long? = null): String =
        retryablePostRequest<String>(
            query = "reader/api/0/mark-all-as-read",
            form =
                mutableListOf(Pair("s", streamId)).apply {
                    sinceTimestamp?.let { add(Pair("ts", it.toString())) }
                },
        )

    companion object {

        const val MAXIMUM_ITEMS_LIMIT = "1000"

        private const val PREFIX = "tag:google.com,2005:reader/item/"

        /*
        || Long form || Short form || notes ||
        || `tag:google.com,2005:reader/item/5d0cfa30041d4348` || `6705009029382226760` || ||
        || `tag:google.com,2005:reader/item/024025978b5e50d2` ||  `162170919393841362` || Long form needs 0-padding ||
        || `tag:google.com,2005:reader/item/fb115bd6d34a8e9f` || `-355401917359550817` || Short form ends up being negative ||
        */
        @OptIn(ExperimentalStdlibApi::class)
        private val hexFormat = HexFormat {
            number {
                minLength = 16
                removeLeadingZeros = false
            }
        }

        val ItemId.isLongId: Boolean
            get() = startsWith(PREFIX) && removePrefix(PREFIX).length == 16

        val ItemId.isShortId: Boolean
            get() = this.toLongOrNull() != null

        fun ItemId.isValidItemId(): Boolean = isLongId || isShortId

        @OptIn(ExperimentalStdlibApi::class)
        val ItemId.longId: String
            get() =
                if (isLongId) {
                    this
                } else {
                    PREFIX + this.toLong(10).toHexString(hexFormat)
                }

        @OptIn(ExperimentalStdlibApi::class)
        val ItemId.shortId: String
            get() =
                if (!isLongId) {
                    this
                } else {
                    this.removePrefix(PREFIX).hexToLong().toString()
                }

        fun ItemId.dbId(accountId: Int) = accountId spacerDollar this

        val String.remoteId
            get() = this.dollarLast()

        fun String.ofFeedIdToStreamId(): String {
            return "feed/$this"
        }

        fun String.ofFeedStreamIdToId(): String {
            return replace("feed/", "")
        }

        fun String.ofCategoryIdToStreamId(): String {
            return "user/-/label/$this"
        }

        private val categoryStreamIdRegex = "user/[^/]+/label/".toRegex()

        fun String.ofCategoryStreamIdToId(): String {
            return replace(categoryStreamIdRegex, "")
        }

        private val instances: ConcurrentHashMap<String, GoogleReaderAPI> = ConcurrentHashMap()

        fun getInstance(
            context: Context,
            serverUrl: String,
            username: String,
            password: String,
            httpUsername: String? = null,
            httpPassword: String? = null,
            clientCertificateAlias: String? = null,
            syncLogger: SyncLogger,
        ): GoogleReaderAPI =
            instances.getOrPut(
                "$serverUrl$username$password$httpUsername$httpPassword$clientCertificateAlias"
            ) {
                GoogleReaderAPI(
                    context,
                    serverUrl,
                    username,
                    password,
                    httpUsername,
                    httpPassword,
                    syncLogger,
                    clientCertificateAlias,
                )
            }

        fun clearInstance() {
            instances.clear()
        }
    }
}

typealias ItemId = String
