package fastcampus.aop_part4_chapter06.data.models.airquality


import com.google.gson.annotations.SerializedName

data class MeasuredValue(
    @SerializedName("coFlag")
    val coFlag: String?,
    @SerializedName("coGrade")
    val coGrade: Grade?,
    @SerializedName("coValue")
    val coValue: String?,
    @SerializedName("dataTime")
    val dataTime: String?,
    @SerializedName("khaiGrade")
    val khaiGrade: Grade?,
    @SerializedName("khaiValue")
    val khaiValue: String?,
    @SerializedName("mangName")
    val mangName: String?,
    @SerializedName("no2Flag")
    val no2Flag: String?,
    @SerializedName("no2Grade")
    val no2Grade: Grade?,
    @SerializedName("no2Value")
    val no2Value: String?,
    @SerializedName("o3Flag")
    val o3Flag: String?,
    @SerializedName("o3Grade")
    val o3Grade: Grade?,
    @SerializedName("o3Value")
    val o3Value: String?,
    @SerializedName("pm10Flag")
    val pm10Flag: String?,
    @SerializedName("pm10Grade")
    val pm10Grade: Grade?,
    @SerializedName("pm10Grade1h")
    val pm10Grade1h: Grade?,
    @SerializedName("pm10Value")
    val pm10Value: String?,
    @SerializedName("pm10Value24")
    val pm10Value24: String?,
    @SerializedName("pm25Flag")
    val pm25Flag: String?,
    @SerializedName("pm25Grade")
    val pm25Grade: Grade?,
    @SerializedName("pm25Grade1h")
    val pm25Grade1h: Grade?,
    @SerializedName("pm25Value")
    val pm25Value: String?,
    @SerializedName("pm25Value24")
    val pm25Value24: String?,
    @SerializedName("so2Flag")
    val so2Flag: String?,
    @SerializedName("so2Grade")
    val so2Grade: Grade?,
    @SerializedName("so2Value")
    val so2Value: String?
)