package com.rokudo.xpense.data.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object ApiClient {

    // For Android emulator use 10.0.2.2; for physical device use your machine's LAN IP
    private const val BASE_URL = "http://192.168.0.169:8080/api/"

    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    fun getToken(): String? = token

    fun clearToken() {
        token = null
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        token?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }
        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Custom Date adapter that handles ISO 8601 LocalDateTime formats from the backend,
     * e.g. "2026-05-17T19:32:34.732221" or "2026-05-17T14:30:00"
     */
    private val dateFormats = arrayOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US)
    )

    private val dateSerializer = object : JsonSerializer<Date>, JsonDeserializer<Date> {
        override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            return JsonPrimitive(fmt.format(src ?: Date()))
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date? {
            val dateStr = json?.asString ?: return null
            for (fmt in dateFormats) {
                try {
                    return fmt.parse(dateStr)
                } catch (_: Exception) { }
            }
            // Last resort: try parsing as epoch millis
            try {
                return Date(dateStr.toLong())
            } catch (_: Exception) { }
            return null
        }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, dateSerializer)
        .create()

    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

