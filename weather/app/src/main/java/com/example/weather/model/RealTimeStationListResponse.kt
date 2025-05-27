package com.example.weather.model


data class RealTimeStationListResponse(
    val response: StationListResponse
)
data class StationListResponse(
    val body: StationListBody
)
data class StationListBody(
    val items: List<StationItem>
)
data class StationItem(
    val stationName: String,
    val addr: String,
    val tm: String
)