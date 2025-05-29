package com.example.weather.network

import com.example.weather.model.RealTimeDustResponse
import com.example.weather.model.RealTimeStationListResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityService {
    // 전국 측정소 목록 조회
    @GET("B552584/ArpltnInforInqireSvc/getMsrstnList")
    fun getStationList(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("returnType")    returnType: String = "JSON",
        @Query("numOfRows")     numOfRows: Int    = 100,
        @Query("pageNo")        pageNo: Int      = 1
    ): Call<RealTimeStationListResponse>

    // 특정 측정소 실시간 미세먼지 농도 조회
    @GET("B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getRealTimeDust(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("stationName")   stationName: String,
        @Query("dataTerm")      dataTerm: String = "daily",
        @Query("numOfRows")     numOfRows: Int    = 1,
        @Query("pageNo")        pageNo: Int      = 1,
        @Query("ver")           ver: String      = "1.3",
        @Query("returnType")    returnType: String = "JSON"
    ): Call<RealTimeDustResponse>

    // 내 주변 측정소 조회
    @GET("B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getNearbyStationList(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("tmX")           tmX: Double,
        @Query("tmY")           tmY: Double,
        @Query("returnType")    returnType: String = "json"
    ): Call<RealTimeStationListResponse>
}
