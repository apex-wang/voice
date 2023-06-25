package io.agora.auikit.service.http

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HttpManager {

    private const val baseUrl = "https://uikit-voiceroom-staging.bj2.agoralab.co/v1/"
    private val retrofit by lazy {
        Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .addInterceptor(CurlInterceptor(object : Logger {
                        override fun log(message: String) {
                            Log.v("Ok2Curl", message)
                        }
                    }))
                    .build()
            )
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    fun <T> getService(clazz: Class<T>): T{
        return retrofit.create(clazz)
    }

}