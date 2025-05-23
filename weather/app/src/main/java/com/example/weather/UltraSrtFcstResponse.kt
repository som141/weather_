
import com.google.gson.annotations.SerializedName

// 최종 호출 결과 래핑
data class UltraSrtFcstResponse(
    @SerializedName("response") val response: UltraResponse
)

data class UltraResponse(
    @SerializedName("header") val header: Header,
    @SerializedName("body")   val body: UltraBody
)

data class Header(
    @SerializedName("resultCode") val resultCode: String,
    @SerializedName("resultMsg")  val resultMsg:  String
)

data class UltraBody(
    @SerializedName("dataType")   val dataType:   String,
    @SerializedName("items")      val items:      ItemsWrapper,
    @SerializedName("pageNo")     val pageNo:     Int,
    @SerializedName("numOfRows")  val numOfRows:  Int,
    @SerializedName("totalCount") val totalCount: Int
)

data class ItemsWrapper(
    @SerializedName("item") val item: List<UltraItem>
)

data class UltraItem(
    @SerializedName("category")  val category:  String, // ex. T1H, SKY, PTY, VEC, WSD …
    @SerializedName("fcstDate")  val fcstDate:  String,
    @SerializedName("fcstTime")  val fcstTime:  String,
    @SerializedName("fcstValue") val fcstValue: String,
    @SerializedName("nx")        val nx:         Int,
    @SerializedName("ny")        val ny:         Int
)
