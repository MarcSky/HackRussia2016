package com.lifepath.swaribrus

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import java.nio.charset.Charset

class FuelHttpService {
    fun textUrl(url: String, parameters: List<Pair<String, Any?>>? = null): Promise<String, Exception> {
        return Fuel.get(url, parameters).promise() then {
            val (request, response, bytes) = it
            String(bytes, Charset.forName(response.contentTypeEncoding))
        }
    }

    fun longPull(url: String): Promise<String, Exception> {
        return Fuel.post(url).promise() then {
            val (request, response, bytes) = it
            String(bytes, Charset.forName(response.contentTypeEncoding))
        }
    }
}


fun Request.promise(): Promise<Triple<Request, Response, ByteArray>, Exception> {
    val deferred = deferred<Triple<Request, Response, ByteArray>, Exception>()
    task { response() } success {
        val (request, response, result) = it
        when(result) {
            is Result.Success -> deferred.resolve(Triple(request, response, result.value))
            is Result.Failure -> deferred.reject(result.error)
        }
    } fail {
        deferred.reject(it)
    }
    return deferred.promise
}

val Response.contentTypeEncoding: String
    get() = contentTypeEncoding()

fun Response.contentTypeEncoding(default: String = "utf-8"): String {
    val contentType: String = httpResponseHeaders["Content-Type"]?.first() ?: return default
    return contentType.substringAfterLast("charset=", default).substringAfter(' ', default)
}
