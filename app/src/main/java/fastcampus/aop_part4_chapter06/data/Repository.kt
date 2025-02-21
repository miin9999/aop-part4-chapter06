package fastcampus.aop_part4_chapter06.data

import android.util.Log
import fastcampus.aop_part4_chapter06.BuildConfig
import fastcampus.aop_part4_chapter06.data.models.airquality.MeasuredValue
import fastcampus.aop_part4_chapter06.data.models.monitoringstation.MonitoringStation
import fastcampus.aop_part4_chapter06.data.services.AirKoreaApiService
import fastcampus.aop_part4_chapter06.data.services.KakaoLocalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object Repository {


    // getTmCoordinates 가 suspend이기 때문에 suspend로 작성
    suspend fun getNearbyMonitoringStation(
        latitude: Double,
        longitude: Double
    ): MonitoringStation? {
        val tmCoordinates = kakaoLocalApiService
            .getTmCoordinates(longitude, latitude)
            .body() // body가 null 이라는 것은 어떤 모종의 이유로 실패했다는 이야기
            ?.documents
            ?.firstOrNull() // 첫번째를 가져오고 없으면 null 가져옴

        // 이 tm좌표를 가지고 근접한 측정소 정보를 가져오는 것임
        val tmX = tmCoordinates?.x
        val tmY = tmCoordinates?.y

        Log.d("tmCoordidocument", tmX.toString())
        Log.d("tmCoordidocument", tmY.toString())


        return airKoreaApiService
            .getNearbyMonitoringStation(tmX!!, tmY!!)
            .body()
            ?.response
            ?.body
            ?.monitoringStations
            ?.minByOrNull { it?.tm ?: Double.MAX_VALUE }
    }

    suspend fun getLatestAirQualityData(stationName: String): MeasuredValue? {
        Log.d("tmCoordidocumentwwwwwwww",stationName)
        return airKoreaApiService
            .getRealtimeAirQualities(stationName)
            .body()
            ?.response
            ?.body
            ?.measuredValues
            ?.firstOrNull()
    }


    private val kakaoLocalApiService: KakaoLocalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Url.KAKAO_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()
    }

    private val airKoreaApiService: AirKoreaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Url.AIR_KOREA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()
    }

    private fun buildHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
}