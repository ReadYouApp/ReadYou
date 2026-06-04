/*
 * Feeder: Android RSS reader app
 * https://gitlab.com/spacecowboy/Feeder
 *
 * Copyright (C) 2022  Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ash.reader.infrastructure.di

import android.annotation.SuppressLint
import android.content.Context
import android.security.KeyChain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.ash.reader.BuildConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.platform.Platform
import java.io.File
import java.net.Socket
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.X509KeyManager
import javax.net.ssl.X509TrustManager

/**
 * Provides singleton [OkHttpClient] for the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object OkHttpClientModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient = cachingHttpClient(
        context = context,
        cacheDirectory = context.cacheDir.resolve("http")
    ).newBuilder()
        .addNetworkInterceptor(UserAgentInterceptor)
        .build()
}

fun cachingHttpClient(
    context: Context,
    cacheDirectory: File? = null,
    cacheSize: Long = 10L * 1024L * 1024L,
    trustAllCerts: Boolean = true,
    connectTimeoutSecs: Long = 30L,
    readTimeoutSecs: Long = 30L,
    clientCertificateAlias: String? = null,
): OkHttpClient {
    val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    if (cacheDirectory != null) {
        builder.cache(Cache(cacheDirectory, cacheSize))
    }

    builder
        .connectTimeout(connectTimeoutSecs, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSecs, TimeUnit.SECONDS)
        .followRedirects(true)

    if (!clientCertificateAlias.isNullOrBlank() || trustAllCerts) {
        builder.setupSsl(context, clientCertificateAlias, trustAllCerts)
    }

    return builder.build()
}

fun OkHttpClient.Builder.setupSsl(
    context: Context,
    clientCertificateAlias: String?,
    trustAllCerts: Boolean
) {
    try {
        val clientKeyManager = clientCertificateAlias?.let { clientAlias ->
            object : X509KeyManager {
                override fun getClientAliases(keyType: String?, issuers: Array<Principal>?) =
                    throw UnsupportedOperationException("getClientAliases")

                override fun chooseClientAlias(
                    keyType: Array<String>?,
                    issuers: Array<Principal>?,
                    socket: Socket?
                ) = clientCertificateAlias

                override fun getServerAliases(keyType: String?, issuers: Array<Principal>?) =
                    throw UnsupportedOperationException("getServerAliases")

                override fun chooseServerAlias(
                    keyType: String?,
                    issuers: Array<Principal>?,
                    socket: Socket?
                ) = throw UnsupportedOperationException("chooseServerAlias")

                override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
                    return if (alias == clientAlias) KeyChain.getCertificateChain(context, clientAlias) else null
                }

                override fun getPrivateKey(alias: String?): PrivateKey? {
                    return if (alias == clientAlias) KeyChain.getPrivateKey(context, clientAlias) else null
                }
            }
        }

        val trustManager = if (trustAllCerts) {
            hostnameVerifier { _, _ -> true }

            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
        } else {
            Platform.get().platformTrustManager()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(arrayOf(clientKeyManager), arrayOf(trustManager), null)
        val sslSocketFactory = sslContext.socketFactory

        sslSocketFactory(sslSocketFactory, trustManager)
    } catch (e: NoSuchAlgorithmException) {
        // ignore
    } catch (e: KeyManagementException) {
        // ignore
    }
}

object UserAgentInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request()
                .newBuilder()
                .header("User-Agent", USER_AGENT_STRING)
                .build()
        )
    }
}

const val USER_AGENT_STRING = BuildConfig.USER_AGENT_STRING