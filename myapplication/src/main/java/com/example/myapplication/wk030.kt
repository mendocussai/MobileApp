package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HomeScreen06() }
    }
}

@Composable
private fun safePainter(resId: Int): Painter {
    return runCatching { painterResource(id = resId) }
        .getOrElse { ColorPainter(Color.LightGray) }
}

@Composable
fun HomeScreen06() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Compose Coffee",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Image(
            painter = safePainter(R.drawable.poo),
            contentDescription = "가게 사진",
            modifier = Modifier.size(width = 260.dp, height = 180.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* TODO: 커피 주문 */ },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
            ) { Text("커피 주문") }

            Button(
                onClick = { /* TODO: 홍차 주문 */ },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
            ) { Text("홍차 주문") }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "위치: 우송대 정문 앞",
            style = MaterialTheme.typography.titleMedium
        )
    }
}


@Preview(showBackground = true, name = "HomeScreenPreview")
@Composable
private fun Preview030() {
    HomeScreen06()
}
