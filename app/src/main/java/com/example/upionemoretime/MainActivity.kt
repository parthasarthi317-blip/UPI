package com.example.upionemoretime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.upionemoretime.navigation.AppNavigation
import com.example.upionemoretime.ui.theme.UPIOneMoreTimeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UPIOneMoreTimeTheme {
                AppNavigation()
            }
        }
    }
}