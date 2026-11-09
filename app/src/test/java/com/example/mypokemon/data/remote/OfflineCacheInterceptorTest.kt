package com.example.mypokemon.data.remote

import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class OfflineCacheInterceptorTest {

    private val interceptor = OfflineCacheInterceptor()
    private val request = Request.Builder().url("https://pokeapi.co/api/v2/pokemon/25").build()

    @Test
    fun `con red devuelve la respuesta sin tocar la caché`() {
        val chain = FakeChain(request) { response(it, 200) }

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        assertEquals(1, chain.requests.size)
    }

    @Test
    fun `sin red responde con la copia de la caché aunque esté caducada`() {
        val chain = FakeChain(request) {
            if (it.cacheControl.onlyIfCached) response(it, 200) else throw IOException("sin red")
        }

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        val cacheRequest = chain.requests.last()
        assertTrue(cacheRequest.cacheControl.onlyIfCached)
        assertEquals(TimeUnit.DAYS.toSeconds(30).toInt(), cacheRequest.cacheControl.maxStaleSeconds)
    }

    @Test
    fun `sin red y sin copia relanza el error de red original`() {
        val networkError = IOException("sin red")
        // OkHttp responde 504 cuando `only-if-cached` no encuentra copia.
        val chain = FakeChain(request) {
            if (it.cacheControl.onlyIfCached) response(it, 504) else throw networkError
        }

        val error = runCatching { interceptor.intercept(chain) }.exceptionOrNull()

        assertSame(networkError, error)
    }

    private fun response(request: Request, code: Int) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("")
        .body("".toResponseBody())
        .build()

    /**
     * `Interceptor.Chain` falso: `handler` decide qué pasa con cada petición (red o
     * caché) y `requests` registra las peticiones que hizo el interceptor.
     */
    private class FakeChain(
        private val request: Request,
        private val handler: (Request) -> Response
    ) : Interceptor.Chain {
        val requests = mutableListOf<Request>()

        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            requests += request
            return handler(request)
        }

        override fun connection(): Connection? = null
        override fun call(): Call = throw UnsupportedOperationException()
        override fun connectTimeoutMillis(): Int = 0
        override fun withConnectTimeout(timeout: Int, unit: TimeUnit) = this
        override fun readTimeoutMillis(): Int = 0
        override fun withReadTimeout(timeout: Int, unit: TimeUnit) = this
        override fun writeTimeoutMillis(): Int = 0
        override fun withWriteTimeout(timeout: Int, unit: TimeUnit) = this
    }
}
