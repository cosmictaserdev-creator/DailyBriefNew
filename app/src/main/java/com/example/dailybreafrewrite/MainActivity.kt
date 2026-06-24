package com.example.dailybreafrewrite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dailybreafrewrite.core.ui.theme.DailyBriefTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyBriefTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DailyBriefApp(
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun DailyBriefApp(modifier: Modifier = Modifier) {
    Text(text = "DailyBrief", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun DailyBriefPreview() {
    DailyBriefTheme {
        DailyBriefApp()
    }
}
