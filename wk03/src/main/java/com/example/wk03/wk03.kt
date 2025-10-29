package com.example.wk03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


data class StopwatchState(
    val elapsedTime: Long = 0L,
    val isRunning: Boolean = false
)

class StopwatchViewModel : ViewModel() {
    private var startTime = 0L
    private var jobActive = false
    private val _state = mutableStateOf(StopwatchState())
    val state: State<StopwatchState> = _state

    fun start() {
        if (_state.value.isRunning) return
        startTime = System.currentTimeMillis() - _state.value.elapsedTime
        _state.value = _state.value.copy(isRunning = true)
        jobActive = true
        updateTimer()
    }

    private fun updateTimer() {
        viewModelScope.launch {
            while (jobActive && _state.value.isRunning) {
                val newTime = System.currentTimeMillis() - startTime
                _state.value = _state.value.copy(elapsedTime = newTime)
                delay(10)
            }
        }
    }

    fun stop() {
        _state.value = _state.value.copy(isRunning = false)
        jobActive = false
    }

    fun reset() {
        _state.value = StopwatchState()
        jobActive = false
    }
}


@Composable
fun StopwatchScreen(viewModel: StopwatchViewModel = viewModel()) {
    val state by viewModel.state

    val seconds = (state.elapsedTime / 1000) % 60
    val minutes = (state.elapsedTime / 60000) % 60
    val millis = (state.elapsedTime % 1000) / 10

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%02d:%02d.%02d".format(minutes, seconds, millis),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { if (state.isRunning) viewModel.stop() else viewModel.start() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRunning)
                            Color(0xFFE74C3C) else Color(0xFF27AE60)
                    )
                ) {
                    Text(if (state.isRunning) "STOP" else "START")
                }
                Button(
                    onClick = { viewModel.reset() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("RESET")
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface {
                    StopwatchScreen()
                }
            }
        }
    }
}