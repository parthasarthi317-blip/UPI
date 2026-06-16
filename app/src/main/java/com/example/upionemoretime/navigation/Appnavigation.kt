package com.example.upionemoretime.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.*
import com.example.upionemoretime.screens.BalanceScreen
import com.example.upionemoretime.screens.HomeScreen
import com.example.upionemoretime.screens.PaymentScreen
import com.example.upionemoretime.screens.RechargeScreen
import com.example.upionemoretime.screens.SettingsScreen
import com.example.upionemoretime.screens.HistoryScreen
import com.example.upionemoretime.screens.StatsScreen
import com.example.upionemoretime.voice.VoiceManager

@Composable
fun AppNavigation(voiceManager: VoiceManager) {

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        voiceManager.updateNavController(navController)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HISTORY) {
            HistoryScreen(navController, voiceManager)
        }

        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                voiceManager = voiceManager
            )
        }

        composable(
            route = Routes.PAYMENT
        ) { backStackEntry ->

            val amount =
                backStackEntry.arguments
                    ?.getString("amount")
                    ?.toIntOrNull()
                    ?: 0

            val receiver =
                backStackEntry.arguments
                    ?.getString("receiver")
                    ?: ""

            PaymentScreen(
                amount = amount,
                receiver = receiver,
                navController = navController,
                voiceManager = voiceManager
            )
        }

        composable(Routes.BALANCE) {
            BalanceScreen(navController, voiceManager)
        }

        composable(route = Routes.RECHARGE_ROUTE) {
                backStackEntry ->
            val mobileNumber =
                backStackEntry.arguments
                    ?.getString("mobileNumber")
                    ?: ""

            val amount =
                backStackEntry.arguments
                    ?.getString("amount")
                    ?.toIntOrNull()
                    ?: 0
            RechargeScreen(
                navController = navController,
                voiceManager = voiceManager,
                initialMobileNumber = mobileNumber,
                initialAmount = amount
            )
        }
        composable(
            route = Routes.RECHARGE
        ) {
            RechargeScreen(
                navController = navController,
                voiceManager = voiceManager
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController, voiceManager)
        }
        composable(Routes.STATS) {
            StatsScreen(navController, voiceManager)
        }
    }
}