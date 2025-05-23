import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weather.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** 시간별 예보를 담는 DTO */
data class HourlyForecast(val time: LocalDateTime, val temperature: String)

class WeatherRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    private val serviceKey = "nVI4pgoe68ebaYhYMSSCyBFeldG0NThgzKEsA6mfpCNJ7jNxG0qbRzeUvUtjN6S42+Ca+Vnp6+Md/NbOJ9Z5Ag=="
    private val nx = 60
    private val ny = 127

    /** “지금” 시점의 초단기예보(기온·하늘·강수·풍향·풍속) → SharedPreferences 저장 */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchUltraShortNow(onComplete: () -> Unit = {}) {
        val ref     = LocalDateTime.now().minusMinutes(45)
        val baseDate= ref.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime= ref.format(DateTimeFormatter.ofPattern("1900"))

        RetrofitClient.apiService.getUltraShortForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<UltraSrtFcstResponse> {
            override fun onResponse(
                call: Call<UltraSrtFcstResponse>,
                resp: Response<UltraSrtFcstResponse>
            ) {
                val items = resp.body()
                    ?.response?.body?.items?.item
                    .orEmpty()

                // 카테고리별 매핑
                val map = items.associateBy { it.category }
                prefs.edit()
                    .putString("weather_temp",     map["T1H"]?.fcstValue ?: "--")
                    .putString("weather_sky",      map["SKY"]?.fcstValue ?: "1")
                    .putString("weather_precip",   map["PTY"]?.fcstValue ?: "0")
                    .putString("weather_wind_dir", map["VEC"]?.fcstValue ?: "--")
                    .putString("weather_wind_spd", map["WSD"]?.fcstValue ?: "--")
                    .apply()

                onComplete()
            }
            override fun onFailure(call: Call<UltraSrtFcstResponse>, t: Throwable) {
                onComplete()
            }
        })
    }

    /** “지금부터 5시간치” 초단기예보 시간별 기온 리스트 반환 */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchHourlyForecastNext5Hours(onComplete: (List<HourlyForecast>) -> Unit) {
        val ref     = LocalDateTime.now().minusMinutes(45)
        val baseDate= ref.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime= ref.format(DateTimeFormatter.ofPattern("HHmm"))
        val now     = LocalDateTime.now()
        val cutoff  = now.plusHours(5)

        RetrofitClient.apiService.getUltraShortForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<UltraSrtFcstResponse> {
            override fun onResponse(
                call: Call<UltraSrtFcstResponse>,
                resp: Response<UltraSrtFcstResponse>
            ) {
                val items = resp.body()
                    ?.response?.body?.items?.item
                    .orEmpty()
                // T1H 항목만, 시간 파싱 후 현재~5시간치 필터
                val list = items
                    .filter { it.category == "T1H" }
                    .mapNotNull {
                        val dt = LocalDateTime.parse(
                            it.fcstDate + it.fcstTime,
                            DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                        )
                        if (dt.isAfter(now) && !dt.isAfter(cutoff))
                            HourlyForecast(dt, it.fcstValue)
                        else null
                    }
                    .sortedBy { it.time }
                onComplete(list)
            }
            override fun onFailure(call: Call<UltraSrtFcstResponse>, t: Throwable) {
                onComplete(emptyList())
            }
        })
    }

    /** 오늘의 단기예보(일 최고·최저 기온) → SharedPreferences 저장 */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchDailyHighLow(onComplete: () -> Unit = {}) {
        val today   = LocalDateTime.now()
        val baseDate= today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime= "0200"  // 단기예보는 0200 기준

        RetrofitClient.apiService.getVillageForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<VilageFcstResponse> {
            override fun onResponse(
                call: Call<VilageFcstResponse>,
                resp: Response<VilageFcstResponse>
            ) {
                val items = resp.body()
                    ?.response?.body?.items?.item
                    .orEmpty()
                val high = items.find { it.category == "TMX" }?.fcstValue ?: "--"
                val low  = items.find { it.category == "TMN" }?.fcstValue ?: "--"
                prefs.edit()
                    .putString("weather_daily_high", high)
                    .putString("weather_daily_low",  low)
                    .apply()
                onComplete()
            }
            override fun onFailure(call: Call<VilageFcstResponse>, t: Throwable) {
                onComplete()
            }
        })
    }
}