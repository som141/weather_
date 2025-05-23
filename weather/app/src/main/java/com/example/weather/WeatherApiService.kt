package com.example.weather.network


import UltraSrtFcstResponse
import VilageFcstResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // 초단기예보 (5시간 뒤 예보 파싱용)
    @GET("getUltraSrtFcst")
    fun getUltraShortForecast(
        @Query("serviceKey") serviceKey: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int,
        @Query("dataType") dataType: String = "JSON",
        @Query("numOfRows") numOfRows: Int = 10000,
        @Query("pageNo") pageNo: Int = 1
    ): Call<UltraSrtFcstResponse>

    // 단기예보 (일 최고·최저 기온 TMX, TMN 파싱용)
    @GET("getVilageFcst")
    fun getVillageForecast(
        @Query("serviceKey") serviceKey: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int,
        @Query("dataType") dataType: String = "JSON",
        @Query("numOfRows") numOfRows: Int = 10000,
        @Query("pageNo") pageNo: Int = 1
    ): Call<VilageFcstResponse>
}
