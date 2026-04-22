package com.stocktracker.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stocktracker.phone.ui.StockTrackerPhoneApp

class MainPhoneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as StockPhoneApp
        setContent {
            StockTrackerPhoneApp(app)
        }
    }
}
