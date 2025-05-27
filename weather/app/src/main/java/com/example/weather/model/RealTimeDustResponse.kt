package com.example.weather.model

import com.google.gson.annotations.SerializedName

/**
 * 대기오염 실시간 측정정보 API 응답 모델
 */

data class RealTimeDustResponse(
    @SerializedName("response")
    val response: DustResponse
)

data class DustResponse(
    val header: DustHeader,
    val body: DustBody
)

data class DustHeader(
    val resultCode: String,
    val resultMsg: String
)

data class DustBody(
    /**
     * items 배열을 바로 받습니다
     */
    val items: List<DustItem>,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int
)

data class DustItem(
    /** 측정 시각 예: "2025-05-27 09:00" */
    val dataTime: String,

    /** 미세먼지(PM10) 값 */
    @SerializedName("pm10Value")
    val pm10: String,

    /** 초미세먼지(PM2.5) 값 */
    @SerializedName("pm25Value")
    val pm25: String

    // 필요 시 so2Value, o3Value 등 추가 가능
)
