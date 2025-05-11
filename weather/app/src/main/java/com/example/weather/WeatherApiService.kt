package com.example.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("getUltraSrtFcst")
    fun getUltraShortForecast(
        @Query("serviceKey") serviceKey: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int,
        @Query("dataType") dataType: String = "JSON",
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1
    ): Call<WeatherResponse>

}
