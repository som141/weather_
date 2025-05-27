package com.example.weather.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    // 2) 미세먼지 API 전용 Retrofit 인스턴스
    private val retrofitAir: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/") // AirQualityService @GET 경로가 전체 REST 경로를 상대적으로 참조
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val airQualityService: AirQualityService by lazy {
        retrofitAir.create(AirQualityService::class.java)
    }
}
