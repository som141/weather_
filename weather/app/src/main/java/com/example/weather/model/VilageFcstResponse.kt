
import com.google.gson.annotations.SerializedName

// 단기예보 응답 모델
data class VilageFcstResponse(
    @SerializedName("response") val response: VillageResponse
)

data class VillageResponse(
    @SerializedName("header") val header: Header,
    @SerializedName("body")   val body:   VillageBody
)

data class VillageBody(
    @SerializedName("dataType")   val dataType:   String,
    @SerializedName("items")      val items:      ItemsWrapper,
    @SerializedName("pageNo")     val pageNo:     Int,
    @SerializedName("numOfRows")  val numOfRows:  Int,
    @SerializedName("totalCount") val totalCount: Int
)

// 재활용 가능: Ultra와 동일한 ItemsWrapper, Header 정의를 공유하셔도 됩니다.

data class VillageItem(
    @SerializedName("category")  val category:  String, // TMX, TMN
    @SerializedName("fcstDate")  val fcstDate:  String,
    @SerializedName("fcstTime")  val fcstTime:  String,
    @SerializedName("fcstValue") val fcstValue: String,
    @SerializedName("nx")        val nx:         Int,
    @SerializedName("ny")        val ny:         Int
)
