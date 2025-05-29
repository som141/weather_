// 1) network/AirQualityService.kt
package com.example.weather.network

import com.example.weather.model.RealTimeDustResponse
import com.example.weather.model.RealTimeStationListResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityService {
    @GET("B552584/ArpltnInforInqireSvc/getMsrstnList")
    fun getStationList(
        @Query("serviceKey") serviceKey: String,
        @Query("returnType") returnType: String = "JSON",
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1
    ): Call<RealTimeStationListResponse>

    @GET("B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getRealTimeDust(
        @Query("serviceKey") serviceKey: String,
        @Query("stationName") stationName: String,
        @Query("dataTerm") dataTerm: String = "daily",
        @Query("numOfRows") numOfRows: Int = 1,
        @Query("pageNo") pageNo: Int = 1,
        @Query("ver") ver: String = "1.3",
        @Query("returnType") returnType: String = "JSON"
    ): Call<RealTimeDustResponse>

    @GET("B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getNearbyStationList(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double,
        @Query("_returnType", encoded = true) returnType: String = "json"
    ): Call<RealTimeStationListResponse>
}
