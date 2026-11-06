package com.bookverse.brewkery

import android.app.Application
import com.bookverse.brewkery.data.CartRepository
import com.bookverse.brewkery.data.MenuRepository
import com.bookverse.brewkery.data.NetworkMenuRepository
import com.bookverse.brewkery.data.OrderRepository
import com.bookverse.brewkery.data.remote.BrewkeryApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class BrewkeryApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}

/** Tiny manual DI container – Hilt would be overkill for four screens. */
class AppContainer {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true // null for a non-null field -> use the default
        isLenient = true
    }

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    private val api: BrewkeryApi = Retrofit.Builder()
        .baseUrl(BrewkeryApi.BASE_URL)
        .client(okHttp)
        // raw.githubusercontent serves text/plain, the converter doesn't care about the response type.
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(BrewkeryApi::class.java)

    val menuRepository: MenuRepository = NetworkMenuRepository(api)
    val cartRepository = CartRepository()
    val orderRepository = OrderRepository()
}
