package com.example.weather

object GridConverter {

    data class GridXY(val x: Int, val y: Int)

    private const val RE = 6371.00877 // 지구 반지름 (km)
    private const val GRID = 5.0      // 격자 간격 (km)
    private const val SLAT1 = 30.0    // 표준 위도 1
    private const val SLAT2 = 60.0    // 표준 위도 2
    private const val OLON = 126.0    // 기준점 경도
    private const val OLAT = 38.0     // 기준점 위도
    private const val XO = 43         // 기준점 X좌표
    private const val YO = 136        // 기준점 Y좌표

    private const val DEGRAD = Math.PI / 180.0

    fun convert(lat: Double, lon: Double): GridXY {
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)

        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn

        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)

        var theta = lon * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = (ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * Math.cos(theta) + YO + 0.5).toInt()

        return GridXY(x, y)
    }
}
