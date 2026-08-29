package com.pal.starktest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pal.starktest.features.app.StarkApp
import com.pal.starktest.ui.theme.StarkTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarkTestTheme {
                StarkApp()
            }
        }
    }
}
