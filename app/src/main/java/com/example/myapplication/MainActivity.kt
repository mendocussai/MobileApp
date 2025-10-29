package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface { ReactionTimerGame() }
            }
        }
    }
}

@Composable
fun ReactionTimerGame() {
    var phase by remember { mutableStateOf(Phase.Ready) } // Ready, Waiting, Go, TooSoon, Result
    var startNanos by remember { mutableStateOf(0L) }
    var lastMs by remember { mutableStateOf<Long?>(null) }
    var bestMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(phase) {
        if (phase == Phase.Waiting) {
            delay(Random.nextLong(1000, 3000))
            phase = Phase.Go
            startNanos = System.nanoTime()
        }
    }

    val (bg, subtitle,title) = when (phase) {
        Phase.Ready   -> Triple(Color(0xFF222222), "Tap to start", "시작을 누르면 랜덤 대기 후 ‘Tap!’")
        Phase.Waiting -> Triple(Color(0xFF8E44AD), "Wait…", "화면이 초록색으로 바뀔 때까지 기다리세요")
        Phase.Go      -> Triple(Color(0xFF2ECC71), "Tap!", "지금 바로 탭!")
        Phase.TooSoon -> Triple(Color(0xFFE74C3C), "Too soon!", "너무 빨랐어요. 다시 시도")
        Phase.Result  -> Triple(Color(0xFF34495E), "${lastMs} ms", bestMs?.let { "BEST: ${it} ms" } ?: "첫 기록!")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .clickable {
                when (phase) {
                    Phase.Ready -> { lastMs = null; phase = Phase.Waiting }
                    Phase.Waiting -> phase = Phase.TooSoon
                    Phase.Go -> {
                        val ms = (System.nanoTime() - startNanos) / 1_000_000
                        lastMs = ms
                        bestMs = bestMs?.let { minOf(it, ms) } ?: ms
                        phase = Phase.Result
                    }
                    Phase.TooSoon, Phase.Result -> phase = Phase.Ready
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, textAlign = TextAlign.Center)
            if (phase == Phase.Result && bestMs != null) {
                Spacer(Modifier.height(24.dp))
                Text("화면을 탭해서 재시작", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }
        }
    }
}

private enum class Phase { Ready, Waiting, Go, TooSoon, Result }

