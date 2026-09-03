package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.presentation.ui.MainApp
import com.example.ui.theme.MyApplicationTheme
import com.example.di.DependencyProvider

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    DependencyProvider.initialize(this)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainApp()
      }
    }
  }
}

