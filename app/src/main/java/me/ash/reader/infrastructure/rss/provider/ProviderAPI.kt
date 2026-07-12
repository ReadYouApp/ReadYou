package me.ash.reader.infrastructure.rss.provider

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.ash.reader.infrastructure.di.UserAgentInterceptor
import me.ash.reader.infrastructure.di.cachingHttpClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

abstract class ProviderAPI(
    context: Context,
    clientCertificateAlias: String?,
    customHeaders: Map<String, String> = emptyMap(),
) {

    protected val client: OkHttpClient = cachingHttpClient(
        context = context,
        clientCertificateAlias = clientCertificateAlias,
    )
        .newBuilder()
        .addNetworkInterceptor(UserAgentInterceptor)
        .apply {
            if (customHeaders.isNotEmpty()) {
                addNetworkInterceptor(CustomHeadersInterceptor(customHeaders))
            }
        }
        .build()

    protected val gson: Gson = GsonBuilder().create()

    protected inline fun <reified T> toDTO(jsonStr: String): T =
        gson.fromJson(jsonStr, T::class.java)!!
}

private class CustomHeadersInterceptor(private val headers: Map<String, String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            headers.forEach { (name, value) -> header(name, value) }
        }.build()
        return chain.proceed(request)
    }
}