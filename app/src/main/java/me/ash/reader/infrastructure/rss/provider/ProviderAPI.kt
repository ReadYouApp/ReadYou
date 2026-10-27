package me.ash.reader.infrastructure.rss.provider

import android.content.Context
import kotlinx.serialization.json.Json
import me.ash.reader.infrastructure.di.UserAgentInterceptor
import me.ash.reader.infrastructure.di.cachingHttpClient
import okhttp3.OkHttpClient

abstract class ProviderAPI(context: Context, clientCertificateAlias: String?) {

    protected val client: OkHttpClient = cachingHttpClient(
        context = context,
        clientCertificateAlias = clientCertificateAlias,
    )
        .newBuilder()
        .addNetworkInterceptor(UserAgentInterceptor)
        .build()

    protected val json: Json = Json { ignoreUnknownKeys = true }

    protected inline fun <reified T> toDTO(jsonStr: String): T =
        json.decodeFromString(jsonStr)
}
