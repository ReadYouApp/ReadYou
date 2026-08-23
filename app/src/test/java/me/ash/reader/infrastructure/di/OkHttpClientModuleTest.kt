package me.ash.reader.infrastructure.di

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OkHttpClientModuleTest {

    @Test
    fun testUserAgentStringIsBrowserCompatible() {
        assertTrue(USER_AGENT_STRING.startsWith("Mozilla/5.0"))
        assertTrue(USER_AGENT_STRING.contains("Mobile"))
        assertTrue(!USER_AGENT_STRING.contains("ReadYou"))
    }

    @Test
    fun testUserAgentInterceptorAppliesDefaultUserAgent() {
        var interceptedRequest: Request? = null

        val fakeChain = object : Interceptor.Chain {
            override fun request(): Request = Request.Builder().url("https://example.com/rss.xml").build()

            override fun proceed(request: Request): Response {
                interceptedRequest = request
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody())
                    .build()
            }

            override fun call(): okhttp3.Call = throw NotImplementedError()
            override fun connection(): okhttp3.Connection? = null
            override fun connectTimeoutMillis(): Int = 0
            override fun readTimeoutMillis(): Int = 0
            override fun writeTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        UserAgentInterceptor.intercept(fakeChain)

        assertEquals(USER_AGENT_STRING, interceptedRequest?.header("User-Agent"))
    }

    @Test
    fun testUserAgentInterceptorPreservesCustomUserAgent() {
        var interceptedRequest: Request? = null
        val customUa = "CustomApp/1.0"

        val fakeChain = object : Interceptor.Chain {
            override fun request(): Request = Request.Builder()
                .url("https://example.com/rss.xml")
                .header("User-Agent", customUa)
                .build()

            override fun proceed(request: Request): Response {
                interceptedRequest = request
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody())
                    .build()
            }

            override fun call(): okhttp3.Call = throw NotImplementedError()
            override fun connection(): okhttp3.Connection? = null
            override fun connectTimeoutMillis(): Int = 0
            override fun readTimeoutMillis(): Int = 0
            override fun writeTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        UserAgentInterceptor.intercept(fakeChain)

        assertEquals(customUa, interceptedRequest?.header("User-Agent"))
    }
}
