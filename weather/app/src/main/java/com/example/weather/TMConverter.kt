package com.example.weather
import kotlin.math.*

// TM(Transverse Mercator) 좌표계 변환기 (EPSG:5181 기준)
object TMConverter {
    // GRS80 타원체 파라미터
    private const val RE = 6378137.0                          // 반장축 (m)
    private const val F = 1.0 / 298.257222101                // 편평률
    private val e2 = 2 * F - F * F                           // 이심률 제곱
    private val e = sqrt(e2)

    // 투영 파라미터
    private val lat0 = 38.0 * PI / 180.0                    // 기준 위도(φ₀)
    private val lon0 = 127.0 * PI / 180.0                   // 기준 중앙자오선(λ₀)
    private const val k0 = 1.0                               // 축척 계수
    private const val FE = 200000.0                          // False Easting (m)
    private const val FN = 500000.0                          // False Northing (m)

    /**
     * 위경도 → TM X,Y
     */
    fun convert(lat: Double, lon: Double): TMCoordinate {
        val φ = lat  * PI / 180.0
        val λ = lon  * PI / 180.0

        // 곡률 반경(N), 보정값 등
        val N = RE / sqrt(1 - e2 * sin(φ).pow(2.0))
        val T = tan(φ).pow(2.0)
        val C = e2 / (1 - e2) * cos(φ).pow(2.0)
        val A = (λ - lon0) * cos(φ)

        // 자오선 호(M)
        val M = RE * (
                (1 - e2/4 - 3*e2*e2/64 - 5*e2*e2*e2/256) * φ
                        - (3*e2/8 + 3*e2*e2/32 + 45*e2*e2*e2/1024) * sin(2*φ)
                        + (15*e2*e2/256 + 45*e2*e2*e2/1024) * sin(4*φ)
                        - (35*e2*e2*e2/3072) * sin(6*φ)
                )

        // 투영식
        val x = FE + k0 * N * (
                A
                        + (1 - T + C) * A.pow(3.0) / 6
                        + (5 - 18*T + T*T + 72*C - 58*e2/(1 - e2)) * A.pow(5.0) / 120
                )
        val y = FN + k0 * (
                M
                        + N * tan(φ) * (
                        A.pow(2.0)/2
                                + (5 - T + 9*C + 4*C*C) * A.pow(4.0) / 24
                                + (61 - 58*T + T*T + 600*C - 330*e2/(1 - e2)) * A.pow(6.0) / 720
                        )
                )

        return TMCoordinate(x, y)
    }
}

/**
 * 변환된 TM 좌표 보관용 데이터 클래스
 */
data class TMCoordinate(
    val x: Double,
    val y: Double
)