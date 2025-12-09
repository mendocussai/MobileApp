package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var healthConnectManager: HealthConnectManager

    private var stepCountData by mutableStateOf<List<Int>>(emptyList())
    private var dailyCaloriesBurnedData by mutableStateOf(0.0)
    private var distanceWalkedData by mutableStateOf(0.0)
    private var activeCaloriesBurnedData by mutableStateOf(0.0)

    private val permissionLauncher =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted: Set<String> ->
            Log.d("HEALTH_SYNC", "권한 요청 결과: $granted")

            if (granted.containsAll(healthConnectManager.permissions)) {
                fetchAndSend { steps, calories, distance, activeCalories ->
                    stepCountData = steps
                    dailyCaloriesBurnedData = calories
                    distanceWalkedData = distance
                    activeCaloriesBurnedData = activeCalories
                }
            } else {
                Log.e("HEALTH_SYNC", "권한 요청 실패")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HEALTH_SYNC", "앱 실행됨")

        // HealthConnectManager 생성
        healthConnectManager = HealthConnectManager(this)

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "실버포션",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Text(text = "걸음수 데이터: ${stepCountData.joinToString()} 보")
                Text(text = "칼로리 소모량: ${"%.2f".format(dailyCaloriesBurnedData)} kcal")
                Text(text = "오늘 걸은 거리: ${"%.2f".format(distanceWalkedData)} m")
                Text(text = "활동 칼로리: ${"%.2f".format(activeCaloriesBurnedData)} kcal")

                Button(onClick = {
                    permissionLauncher.launch(healthConnectManager.permissions)
                }) {
                    Text(text = "데이터 가져오기 및 서버 전송")
                }
            }
        }
    }

    private fun fetchAndSend(
        onDataFetched: (List<Int>, Double, Double, Double) -> Unit
    ) {

        Log.d("HEALTH_SYNC", "fetchAndSend 실행됨")

        lifecycleScope.launch {
            try {
                // 걸음수
                val stepRecords = healthConnectManager.readStepCounts()
                val stepData = stepRecords.map { it.count.toInt() }

                // 총 소모 칼로리
                val caloriesRecords = healthConnectManager.readCaloriesBurned()
                val dailyCalories = caloriesRecords.sumOf { it.energy.inCalories }

                // 심박수
                val heartRecords = healthConnectManager.readHeartRates()
                val heartRateData = heartRecords.map {
                    val sample = it.samples.firstOrNull()
                    HeartRateData(
                        bpm = sample?.beatsPerMinute?.toDouble() ?: 0.0,
                        time = sample?.time.toString()
                    )
                }

                // 걸은 거리
                val distanceRecords = healthConnectManager.readDistanceWalked()
                val totalDistance = distanceRecords.sumOf { it.distance.inMeters }

                // 활동 칼로리
                val activeRecords = healthConnectManager.readActiveCaloriesBurned()
                val activeCalories = activeRecords.sumOf { it.energy.inCalories }

                Log.d("HEALTH_SYNC", "걸음수: ${stepData.joinToString()}")
                Log.d("HEALTH_SYNC", "심박수: ${heartRecords.size}")
                Log.d("HEALTH_SYNC", "총 소모 칼로리: $dailyCalories")
                Log.d("HEALTH_SYNC", "오늘 걸은 거리: $totalDistance")
                Log.d("HEALTH_SYNC", "활동 칼로리: $activeCalories")

                onDataFetched(stepData, dailyCalories, totalDistance, activeCalories)

                // HealthData 객체 생성
                val healthData = HealthData(
                    stepData = stepData,
                    heartRateData = heartRateData,
                    caloriesBurnedData = dailyCalories,
                    distanceWalked = totalDistance,
                    activeCaloriesBurned = activeCalories
                )

                // 서버로 전송
                RetrofitClient.apiService.sendHealthData(healthData)

            } catch (e: Exception) {
                Log.e("HEALTH_SYNC", "에러 발생", e)
            }
        }
    }
}
