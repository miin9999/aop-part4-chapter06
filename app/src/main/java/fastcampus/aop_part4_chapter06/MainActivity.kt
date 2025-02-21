package fastcampus.aop_part4_chapter06

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import fastcampus.aop_part4_chapter06.data.Repository
import fastcampus.aop_part4_chapter06.data.models.airquality.Grade
import fastcampus.aop_part4_chapter06.data.models.airquality.MeasuredValue
import fastcampus.aop_part4_chapter06.data.models.monitoringstation.MonitoringStation
import fastcampus.aop_part4_chapter06.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val scope = MainScope()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindViews()
        initVariables()
        // 앱을 시작하자마자 퍼미션 요청
        requestLocationPermissions()

    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.cancel()
        scope.cancel()
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 요청한 코드가 맞는지 확인
        val locationPermissionGranted =
            requestCode == REQUEST_ACCESS_LOCATION_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED


        Log.d("peroaird", "Location Permission Requested: $requestCode, granted: ${grantResults[0] == PackageManager.PERMISSION_GRANTED}")



        val backgroundLocationPermissionGranted =
            requestCode == REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED


        Log.d("peroadvdvird", "Background Location Permission Requested: $requestCode, granted: ${grantResults[0] == PackageManager.PERMISSION_GRANTED}")



        // 안드 버전 11부터 아래처럼 처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!backgroundLocationPermissionGranted) {
                // 권한이 없을 경우 백그라운드 퍼미션 요청
                requestBackgroundLocationPermissions()
            } else {
                fetchAirQualityData()

            }
        } else {
            if (!locationPermissionGranted) {
                // 아닐 경우 앱 종료
                finish()
            } else {
                // 퍼미션 부여 시,
                // 위치 정보를 가지고 api 구조의 스타트를 끊으면 된다 (notion 필기에 있는 그 구조 그림 참고)
                fetchAirQualityData()


            }
        }


    }

    private fun bindViews() {
        binding.refresh.setOnRefreshListener {
            fetchAirQualityData()
        }
    }


    private fun initVariables() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS
        )
    }


    @SuppressLint("MissingPermission")
    private fun fetchAirQualityData() {

        // fetchData
        cancellationTokenSource = CancellationTokenSource()


        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location ->


            Log.d("LOcationLotion", location.longitude.toString())
            Log.d("LOcationLotion", location.latitude.toString())
            // 여기부터가 실제로 api를 호출하는 부분

            scope.launch {


                // 에러 discription 이 발생 후 재시도할 때는 지워 줘야 하기 때문에 GONE 처리
                binding.errorDescriptionTextView.visibility = View.GONE


                try {
                    val monitoringStation =
                        Repository.getNearbyMonitoringStation(location.latitude, location.longitude)

                    Log.d("monitoringStation", "stationName: ${monitoringStation?.stationName},tm: ${monitoringStation?.tm}")


                    val measuredValue =
                        Repository.getLatestAirQualityData(monitoringStation!!.stationName!!)

                    Log.d("measuredValue", measuredValue.toString())


                    displayAirQualityData(monitoringStation, measuredValue!!)

                } catch (exception: Exception) {
                    binding.errorDescriptionTextView.visibility = View.VISIBLE
                    binding.contentsLayout.alpha = 0F

                    Log.d("exceptionexceptionexception", exception.toString())
                } finally {
                    // 모든 게 끝났을 때
                    // exception 발생 여부와 상관없이 작동하는 부분, 로딩 프로그래스는 닫기

                    binding.progressBar.visibility = View.GONE
                    binding.refresh.isRefreshing = false
                    Log.d("finallyfinally", "finally")


                }
            }


        }.addOnFailureListener { e ->
            Log.e("LocationError", "위치 요청 실패: ${e.message}")
            // 실패 시 다시 시도하는 로직 추가
        }


    }


    @SuppressLint("SetTextI18n")
    fun displayAirQualityData(monitoringStation: MonitoringStation, measuredValue: MeasuredValue) {

        binding.contentsLayout.animate()
            .alpha(1F)
            .start()

        binding.measuringStationNameTextView.text = monitoringStation.stationName
        binding.measuringStationAddressTextView.text = monitoringStation.addr

        (measuredValue.khaiGrade ?: Grade.UNKNOWN).let { grade ->
            binding.root.setBackgroundResource(grade.colorResId)
            binding.totalGradeLabelTextView.text = grade.label
            binding.totalGradeEmojiTextView.text = grade.emoji
        }

        with(measuredValue) {
            binding.fineDustInformationTextView.text =
                "미세먼지: $pm10Value ㎍/㎥ ${(pm10Grade ?: Grade.UNKNOWN).emoji} "
            binding.ultraFineDustInformationTextView.text =
                "초미세먼지: $pm25Value ㎍/㎥ ${(pm25Grade ?: Grade.UNKNOWN).emoji}"

            with(binding.so2Item) {
                labelTextView.text = "아황산가스"
                gradeTextView.text = (so2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$so2Value ppm"
            }
            with(binding.coItem) {
                labelTextView.text = "일산화탄소"
                gradeTextView.text = (coGrade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$coValue ppm"
            }
            with(binding.o3Item) {
                labelTextView.text = "오존"
                gradeTextView.text = (o3Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$o3Value ppm"
            }
            with(binding.no2Item) {
                labelTextView.text = "이산화질소"
                gradeTextView.text = (no2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$no2Value ppm"
            }
        }

    }

    companion object {

        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
        private const val REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS = 101

    }
}